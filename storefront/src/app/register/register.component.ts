import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

// A custom validator: runs on the whole group and returns an error object or null.
function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  return group.get('password')?.value === group.get('confirmPassword')?.value ? null : { mismatch: true };
}

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule], // gives us [formGroup], formControlName, etc.
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);

  // Signals hold UI state; the template re-renders when they change.
  submitting = signal(false);
  message = signal<{ ok: boolean; text: string } | null>(null);

  // The form model lives in code (reactive forms). Each control = [initial value, validators].
  form = this.fb.nonNullable.group(
    {
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]], // matches backend @Size(min = 8)
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch },
  );

  // Show errors only once the user has touched a field (or tried to submit).
  invalid(name: 'fullName' | 'email' | 'password' | 'confirmPassword') {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || c.dirty);
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { fullName, email, password } = this.form.getRawValue();
    this.submitting.set(true);
    this.message.set(null);

    // POST /auth/register -> proxied to the API gateway (see proxy.conf.json).
    this.http.post<{ token: string; email: string }>('/auth/register', { fullName, email, password }).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.message.set({ ok: true, text: `Account created for ${res.email}!` });
        this.form.reset();
      },
      error: (err) => {
        this.submitting.set(false);
        this.message.set({ ok: false, text: err.error?.message ?? 'Registration failed. Is the backend running?' });
      },
    });
  }
}
