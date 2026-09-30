"""A guided tour of the finance toolkit.

Run it with:   python demo.py

Each section asks one everyday money question, answers it with the toolkit,
and prints a short takeaway so the numbers mean something.
"""

from finance import investing, loans, time_value


def heading(title):
    print()
    print("=" * 64)
    print(title)
    print("=" * 64)


def money(x):
    # Thousands separators and 2 decimals: 12345.6 -> "12,345.60"
    return f"{x:,.2f}"


def pct(x):
    return f"{x * 100:.2f}%"


def compounding():
    heading("1. Compound interest: why starting early matters")
    # Same total deposit (1,000), same rate (8%), different time in the market.
    for years in (10, 20, 30):
        fv = time_value.future_value(1_000, 0.08, years)
        print(f"  1,000 invested for {years:>2} years at 8% -> {money(fv)}")
    print("  Takeaway: the last 10 years add more than the first 20 combined,")
    print("  because later growth is interest earned on interest.")

    shortcut, exact = time_value.years_to_double(0.08)
    print(f"\n  Rule of 72 says money doubles in {shortcut:.1f} years at 8%;")
    print(f"  the exact answer is {exact:.2f} years. Close enough for mental math.")


def savings_goal():
    heading("2. Savings goal: how much per month?")
    goal, rate, years = 500_000, 0.10, 10
    monthly = time_value.contribution_needed(goal, rate, years)
    deposited = monthly * 12 * years
    print(f"  Goal: {money(goal)} in {years} years, assuming a 10% annual return")
    print(f"  Save {money(monthly)} per month")
    print(f"  You deposit {money(deposited)}; growth supplies the other "
          f"{money(goal - deposited)}.")


def inflation():
    heading("3. Inflation: nominal vs real return")
    nominal, infl = 0.07, 0.05
    real = time_value.real_return(nominal, infl)
    print(f"  A {pct(nominal)} fixed deposit with {pct(infl)} inflation")
    print(f"  grows your *purchasing power* by only {pct(real)} a year.")

    # Present value in reverse: what will today's 100,000 buy in 20 years?
    future_power = time_value.present_value(100_000, infl, 20)
    print(f"  100,000 kept in cash for 20 years buys what {money(future_power)}")
    print("  buys today.")


def loan_walkthrough():
    heading("4. Loans: where your EMI actually goes")
    amount, rate, years = 2_000_000, 0.09, 20
    emi = loans.monthly_payment(amount, rate, years)
    schedule = loans.amortization_schedule(amount, rate, years)
    interest = loans.total_interest(amount, rate, years)

    print(f"  Loan {money(amount)} at {pct(rate)} for {years} years")
    print(f"  EMI: {money(emi)}   Total interest: {money(interest)}")
    print()
    print(f"  {'Month':>5} {'Payment':>12} {'Interest':>12} "
          f"{'Principal':>12} {'Balance':>14}")
    # Show the start and end of the loan to make the shift obvious.
    for row in schedule[:3] + schedule[-3:]:
        print(f"  {row['month']:>5} {money(row['payment']):>12} "
              f"{money(row['interest']):>12} {money(row['principal']):>12} "
              f"{money(row['balance']):>14}")
    print("  Takeaway: in month 1 most of the EMI is interest; by the end,")
    print("  almost all of it pays down the loan.")

    extra = 5_000
    months, interest_with_extra = loans.payoff_with_extra(amount, rate, years, extra)
    print(f"\n  Paying {money(extra)} extra every month:")
    print(f"    finishes in {months // 12} years {months % 12} months "
          f"instead of {years} years")
    print(f"    saves {money(interest - interest_with_extra)} in interest")


def project_decision():
    heading("5. Should we invest? NPV and IRR")
    # Spend 100,000 today, receive 30,000 a year for 5 years.
    flows = [-100_000, 30_000, 30_000, 30_000, 30_000, 30_000]
    required = 0.10
    value = investing.npv(required, flows)
    rate = investing.irr(flows)
    print(f"  Cash flows: {flows}")
    print(f"  NPV at a {pct(required)} required return: {money(value)}")
    print(f"  IRR (the project's own return): {pct(rate)}")
    verdict = "invest" if value > 0 else "skip it"
    print(f"  NPV is {'positive' if value > 0 else 'negative'} and IRR "
          f"{'beats' if rate > required else 'misses'} 10% -> {verdict}.")


def risk_and_return():
    heading("6. Risk vs return: two funds compared")
    # Twelve months of made-up monthly returns for illustration.
    steady = [0.015, -0.010, 0.012, 0.008, -0.012, 0.014,
              0.010, -0.006, 0.011, 0.009, -0.008, 0.013]
    wild = [0.06, -0.04, 0.05, -0.03, 0.07, -0.05,
            0.04, 0.02, -0.06, 0.08, -0.02, 0.03]

    for name, returns in (("Steady fund", steady), ("Wild fund", wild)):
        # Turn returns into a price path starting at 100 for drawdown.
        prices = [100.0]
        for r in returns:
            prices.append(prices[-1] * (1 + r))
        growth = investing.cagr(prices[0], prices[-1], 1)
        vol = investing.volatility(returns)
        sharpe = investing.sharpe_ratio(returns, risk_free_annual=0.04)
        dd = investing.max_drawdown(prices)
        print(f"  {name:<12} return {pct(growth):>7}  volatility {pct(vol):>7}  "
              f"Sharpe {sharpe:>5.2f}  worst drop {pct(dd):>7}")
    print("  Takeaway: the wild fund may earn more, but the Sharpe ratio shows")
    print("  how much return you get for each unit of risk you sit through.")


if __name__ == "__main__":
    compounding()
    savings_goal()
    inflation()
    loan_walkthrough()
    project_decision()
    risk_and_return()
    print()
