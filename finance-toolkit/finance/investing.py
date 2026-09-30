"""Investing: judging projects and measuring how an investment has behaved.

Two families of tools live here:

1. **Deciding whether to invest** -- ``npv`` and ``irr`` compare money you put
   in against money you expect to get back.
2. **Judging what happened** -- ``cagr``, ``volatility``, ``sharpe_ratio`` and
   ``max_drawdown`` describe return and risk from a price/return history.

Cash-flow sign convention: money you *pay out* is negative, money you
*receive* is positive. The first flow (index 0) happens today.
"""

import math


def npv(rate, cash_flows):
    """Net Present Value: today's value of a stream of cash flows.

    Formula:  NPV = sum( CF_t / (1 + r) ** t )   for t = 0, 1, 2, ...

    Each future cash flow is discounted back to today (see
    ``time_value.present_value``) and then everything is added up.

    How to read the result:
        NPV > 0  -> the investment earns more than the discount rate: worth it
        NPV < 0  -> it earns less than you could get elsewhere: skip it
    """
    return sum(cf / (1 + rate) ** t for t, cf in enumerate(cash_flows))


def irr(cash_flows, low=-0.99, high=10.0, tolerance=1e-9):
    """Internal Rate of Return: the discount rate that makes NPV equal zero.

    In plain words: "what yearly return does this investment effectively
    earn?" Compare it with your required return -- if IRR is higher, invest.

    There is no closed-form formula, so we use **bisection**:
        * NPV falls as the rate rises, so a root exists between a rate where
          NPV is positive and one where it is negative.
        * Repeatedly test the midpoint and keep whichever half still contains
          the sign change, until the bracket is tiny.

    Raises ValueError when no sign change exists in the search range, e.g. all
    cash flows have the same sign (there is no "return" without money going in
    *and* coming out).
    """
    f_low, f_high = npv(low, cash_flows), npv(high, cash_flows)
    if f_low * f_high > 0:
        raise ValueError(
            "IRR is undefined: cash flows need both money out and money in"
        )

    for _ in range(200):  # 200 halvings is far more precision than needed
        mid = (low + high) / 2
        f_mid = npv(mid, cash_flows)
        if abs(f_mid) < tolerance or (high - low) / 2 < tolerance:
            return mid
        # Keep the half of the interval where the sign still flips.
        if f_low * f_mid < 0:
            high = mid
        else:
            low, f_low = mid, f_mid
    return (low + high) / 2


def cagr(start_value, end_value, years):
    """Compound Annual Growth Rate: the smooth yearly rate that links two values.

    Formula:  CAGR = (end / start) ** (1 / years) - 1

    Real investments go up and down. CAGR pretends they grew at one constant
    rate, which makes different investments easy to compare. Going from
    10,000 to 20,000 in 5 years is a CAGR of about 14.9%, however bumpy the
    ride was.
    """
    return (end_value / start_value) ** (1 / years) - 1


def _mean(values):
    return sum(values) / len(values)


def volatility(returns, periods_per_year=12):
    """Annualised volatility: how wildly returns swing (a common measure of risk).

    Steps:
        1. Take the *sample* standard deviation of the periodic returns
           (divide by n-1 because we only see a sample of history).
        2. Scale to a yearly figure by multiplying by sqrt(periods per year).
           Variance adds up across independent periods, so standard deviation
           grows with the *square root* of time -- not linearly.

    Higher volatility = a bumpier ride and a wider range of possible outcomes.
    """
    if len(returns) < 2:
        raise ValueError("need at least two returns to measure volatility")
    m = _mean(returns)
    variance = sum((r - m) ** 2 for r in returns) / (len(returns) - 1)
    return math.sqrt(variance) * math.sqrt(periods_per_year)


def sharpe_ratio(returns, risk_free_annual=0.0, periods_per_year=12):
    """Sharpe ratio: return earned per unit of risk taken.

    Formula:  Sharpe = (average return - risk-free return) / volatility

    Both numerator and denominator are annualised here. The "risk-free rate"
    is what you'd earn with almost no risk (e.g. government bills), so the
    numerator is the *extra* return you were paid for taking risk.

    Rough guide: below 1 is meh, 1-2 is good, above 2 is excellent.
    Use it to compare two investments with different risk levels; the higher
    Sharpe is the better risk-adjusted deal.
    """
    annual_mean = _mean(returns) * periods_per_year
    vol = volatility(returns, periods_per_year)
    if vol == 0:
        raise ValueError("volatility is zero; Sharpe ratio is undefined")
    return (annual_mean - risk_free_annual) / vol


def max_drawdown(prices):
    """Worst peak-to-trough fall, as a negative fraction (-0.25 means -25%).

    Answers the gut-level question: "at my worst moment, how much would I have
    lost?" We walk the price history, remembering the highest price so far
    (the "peak"), and measure how far each later price sits below that peak.
    The deepest such gap is the maximum drawdown.
    """
    peak = prices[0]
    worst = 0.0
    for price in prices:
        peak = max(peak, price)          # new high-water mark
        drawdown = price / peak - 1      # 0 at a peak, negative below it
        worst = min(worst, drawdown)
    return worst
