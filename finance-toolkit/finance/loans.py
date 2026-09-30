"""Loans: how an EMI is calculated and where each payment actually goes.

A standard loan (home, car, personal) is repaid with equal monthly payments
(an "EMI" in India, a "mortgage payment" in the US). The payment never
changes, but its *make-up* does:

* Early on, the balance is large, so most of the payment is **interest**.
* Later, the balance is small, so most of the payment goes to **principal**.

That shift is the main thing to understand about amortization, and
``amortization_schedule`` lets you see it row by row.
"""


def monthly_payment(loan_amount, annual_rate, years):
    """The fixed monthly payment that pays a loan off in exactly ``years``.

    Formula:  PMT = L * i / (1 - (1 + i) ** -n)

        L = loan amount, i = monthly rate (annual / 12),
        n = number of monthly payments (years * 12)

    Intuition: the loan is worth exactly the present value of all its future
    payments. This formula is that idea solved for the payment.
    """
    n = years * 12
    i = annual_rate / 12
    if i == 0:
        # An interest-free loan is just the amount split evenly.
        return loan_amount / n
    return loan_amount * i / (1 - (1 + i) ** -n)


def _simulate(loan_amount, annual_rate, payment, max_months):
    """Shared month-by-month engine behind the public loan functions.

    How each month works:
        1. interest   = current balance * monthly rate
        2. principal  = payment - interest   (the part that shrinks the debt)
        3. balance    = balance - principal

    Money rounding: real lenders work in whole cents, so the payment and each
    month's interest are rounded to 2 decimals. Those roundings leave a few
    cents over (or under) by the end, so the *final* payment is adjusted to
    clear exactly what remains -- just like a real bank statement. A payment
    is final when it can cover the whole balance, or when the loan term
    (``max_months``) is up.
    """
    i = annual_rate / 12
    balance = float(loan_amount)
    rows = []
    month = 0
    while balance > 0:
        month += 1
        interest = round(balance * i, 2)
        if balance + interest <= payment or month == max_months:
            # Final month: pay off everything that is left.
            this_payment = round(balance + interest, 2)
            principal = balance
            balance = 0.0
        else:
            this_payment = payment
            principal = round(payment - interest, 2)
            balance = round(balance - principal, 2)
        rows.append({
            "month": month,
            "payment": this_payment,
            "interest": interest,
            "principal": principal,
            "balance": balance,
        })
    return rows


def amortization_schedule(loan_amount, annual_rate, years):
    """Month-by-month breakdown of a loan.

    Returns a list of dicts with keys:
        month, payment, interest, principal, balance

    Reading it top to bottom shows the interest part of each payment
    shrinking while the principal part grows.
    """
    payment = round(monthly_payment(loan_amount, annual_rate, years), 2)
    return _simulate(loan_amount, annual_rate, payment, max_months=years * 12)


def total_interest(loan_amount, annual_rate, years):
    """Total interest paid over the life of the loan (the true cost of borrowing)."""
    schedule = amortization_schedule(loan_amount, annual_rate, years)
    return round(sum(row["interest"] for row in schedule), 2)


def payoff_with_extra(loan_amount, annual_rate, years, extra_per_month):
    """See what an extra monthly payment does to the loan.

    Any extra money goes straight to principal, so next month's interest is
    calculated on a smaller balance. The snowball effect shortens the loan and
    cuts total interest, often by a surprising amount.

    Returns ``(months_taken, total_interest_paid)``.
    """
    base = round(monthly_payment(loan_amount, annual_rate, years), 2)
    rows = _simulate(loan_amount, annual_rate, base + extra_per_month,
                     max_months=years * 12)
    return len(rows), round(sum(row["interest"] for row in rows), 2)
