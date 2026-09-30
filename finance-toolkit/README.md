# Finance Toolkit

A small, heavily commented Python project for learning the core ideas of
personal and corporate finance. Every function shows its formula, explains
the intuition in plain English, and is pinned by a test with a hand-checkable
answer.

Pure standard library: nothing to install.

## Run it

```bash
cd finance-toolkit
python demo.py              # guided tour with printed takeaways
python -m unittest -v       # 24 tests
```

## What's inside

| Module | Question it answers | Functions |
|---|---|---|
| `finance/time_value.py` | What is money worth over time? | `future_value`, `present_value`, `future_value_of_contributions`, `contribution_needed`, `years_to_double`, `real_return` |
| `finance/loans.py` | What does a loan really cost? | `monthly_payment`, `amortization_schedule`, `total_interest`, `payoff_with_extra` |
| `finance/investing.py` | Is this investment worth it, and how risky is it? | `npv`, `irr`, `cagr`, `volatility`, `sharpe_ratio`, `max_drawdown` |

## Key ideas in one line each

- **Compounding**: you earn interest on your interest, so growth speeds up over time.
- **Discounting (present value)**: future money is worth less today, because today's money could be earning.
- **Rule of 72**: years to double ≈ 72 ÷ interest rate in %.
- **Real return**: what's left after inflation, `(1 + nominal) / (1 + inflation) − 1`.
- **EMI / amortization**: the payment stays fixed; early payments are mostly interest and later ones mostly principal.
- **NPV**: add up all cash flows in today's money. If the total is positive, the investment beats your required return.
- **IRR**: the return an investment effectively earns, i.e. the rate where NPV = 0.
- **CAGR**: the smooth yearly growth rate between a start value and an end value.
- **Volatility**: how much returns swing; it scales with √time, not time.
- **Sharpe ratio**: extra return earned per unit of risk.
- **Max drawdown**: the worst peak-to-trough loss you would have lived through.

## Conventions

- Rates are decimals: `0.05` means 5%.
- In `investing.py`, money you pay out is negative and money you receive is positive.
- Loan amounts are rounded to the cent each month, and the final payment clears
  any leftover cents, the same way a bank statement does.

## Example

```python
from finance import loans, time_value

loans.monthly_payment(200_000, 0.06, 30)      # 1199.10
time_value.contribution_needed(50_000, 0.06, 5)  # ~716.64 a month
```

*For learning only. This is not financial advice.*
