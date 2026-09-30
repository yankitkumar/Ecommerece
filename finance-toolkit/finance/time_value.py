"""Time value of money: why a rupee/dollar today is worth more than one tomorrow.

Everything in personal finance builds on one idea: money can earn interest, so
money you have *now* is worth more than the same amount promised *later*.
This module contains the handful of formulas that express that idea.

Conventions used throughout this package
----------------------------------------
* Rates are plain decimals: 5% is written ``0.05`` (not ``5``).
* "annual_rate" is the *nominal* yearly rate. If interest compounds more than
  once a year, the rate is divided evenly across the periods (12 periods at a
  nominal 6% means 0.5% per month).
* Money-in is negative and money-out is positive only in ``investing.py``
  (cash-flow convention). Here, amounts are just plain positive numbers.
"""

import math


def future_value(principal, annual_rate, years, compounds_per_year=1):
    """What a lump sum grows into after earning compound interest.

    Formula:  FV = PV * (1 + r/m) ** (m * t)

        PV = principal, r = nominal annual rate,
        m  = compounding periods per year, t = years

    "Compound" means you earn interest on your interest. That is why the curve
    bends upward instead of growing in a straight line.

    >>> round(future_value(1000, 0.05, 10), 2)
    1628.89
    """
    periods = compounds_per_year * years
    rate_per_period = annual_rate / compounds_per_year
    return principal * (1 + rate_per_period) ** periods


def present_value(future_amount, annual_rate, years, compounds_per_year=1):
    """What a future sum is worth in today's money ("discounting").

    This is ``future_value`` run backwards:  PV = FV / (1 + r/m) ** (m * t)

    The rate used here is called the *discount rate*. A higher discount rate
    makes the future amount worth less today, because you are assuming you
    could have earned more by investing elsewhere.

    >>> round(present_value(1628.89, 0.05, 10), 2)
    1000.0
    """
    periods = compounds_per_year * years
    rate_per_period = annual_rate / compounds_per_year
    return future_amount / (1 + rate_per_period) ** periods


def future_value_of_contributions(payment, annual_rate, years, payments_per_year=12):
    """What a *regular* deposit grows into (an "ordinary annuity").

    Think of a monthly SIP / recurring deposit. Each deposit is made at the end
    of its period, and each one compounds for a different length of time (the
    first deposit grows the longest, the last one not at all).

    Formula:  FV = PMT * ((1 + i) ** n - 1) / i

        i = rate per period, n = total number of payments

    When the rate is 0 there is no growth, so the answer is simply the sum of
    the deposits. We handle that separately to avoid dividing by zero.
    """
    n = payments_per_year * years
    i = annual_rate / payments_per_year
    if i == 0:
        return payment * n
    return payment * ((1 + i) ** n - 1) / i


def contribution_needed(goal, annual_rate, years, payments_per_year=12):
    """How much to save each period to reach ``goal`` -- the inverse of the above.

    Useful for questions like "I want 50,000 in 5 years; what must I put aside
    every month?". We solve the annuity formula for PMT:

        PMT = FV * i / ((1 + i) ** n - 1)
    """
    n = payments_per_year * years
    i = annual_rate / payments_per_year
    if i == 0:
        return goal / n
    return goal * i / ((1 + i) ** n - 1)


def years_to_double(annual_rate):
    """How long an investment takes to double at a given annual rate.

    Returns a pair ``(rule_of_72, exact)``.

    * The **Rule of 72** is a mental-math shortcut: years ~= 72 / (rate in %).
      At 8% that is 72 / 8 = 9 years.
    * The **exact** answer solves (1 + r) ** t = 2  =>  t = ln(2) / ln(1 + r).

    Comparing the two shows the shortcut is very close for everyday rates.
    """
    if annual_rate <= 0:
        raise ValueError("annual_rate must be positive to ever double")
    shortcut = 72 / (annual_rate * 100)
    exact = math.log(2) / math.log(1 + annual_rate)
    return shortcut, exact


def real_return(nominal_rate, inflation_rate):
    """Your return after inflation eats into it (the Fisher equation).

    Formula:  real = (1 + nominal) / (1 + inflation) - 1

    A common shortcut is ``nominal - inflation``, but that slightly overstates
    the result. Example: earning 7% while prices rise 4% leaves you with about
    2.88% more purchasing power, not 3%.
    """
    return (1 + nominal_rate) / (1 + inflation_rate) - 1
