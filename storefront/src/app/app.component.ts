import { Component } from '@angular/core';
import { RegisterComponent } from './register/register.component';

@Component({
  selector: 'app-root',
  imports: [RegisterComponent],
  template: `<main><app-register /></main>`,
  styles: [`main { max-width: 420px; margin: 3rem auto; font-family: sans-serif; }`],
})
export class AppComponent {}
