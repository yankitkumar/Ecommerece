"""Tests that pin each formula to a known, hand-checkable answer.

Run from the finance-toolkit folder with:   python -m unittest -v
"""

import unittest

from finance import investing, loans, time_value


class TimeValueTests(unittest.TestCase):
    def test_future_value_annual(self):
        # 1000 * 1.05**10 = 1628.894...
        self.assertAlmostEqual(time_value.future_value(1000, 0.05, 10), 1628.89, places=2)

    def test_monthly_compounding_beats_annual(self):
        annual = time_value.future_value(1000, 0.12, 1, compounds_per_year=1)
        monthly = time_value.future_value(1000, 0.12, 1, compounds_per_year=12)
        self.assertAlmostEqual(monthly, 1126.83, places=2)
        self.assertGreater(monthly, annual)

    def test_present_value_undoes_future_value(self):
        fv = time_value.future_value(2500, 0.07, 8, compounds_per_year=4)
        self.assertAlmostEqual(time_value.present_value(fv, 0.07, 8, 4), 2500)

    def test_contribution_needed_inverts_annuity(self):
        pmt = time_value.contribution_needed(50_000, 0.06, 5)
        self.assertAlmostEqual(
            time_value.future_value_of_contributions(pmt, 0.06, 5), 50_000
        )

    def test_zero_rate_is_just_addition(self):
        self.assertEqual(time_value.future_value_of_contributions(100, 0, 1), 1200)
        self.assertEqual(time_value.contribution_needed(1200, 0, 1), 100)

    def test_rule_of_72_close_to_exact(self):
        shortcut, exact = time_value.years_to_double(0.08)
        self.assertEqual(shortcut, 9)
        self.assertAlmostEqual(exact, 9.006, places=3)

    def test_years_to_double_rejects_non_positive_rate(self):
        with self.assertRaises(ValueError):
            time_value.years_to_double(0)

    def test_real_return(self):
        self.assertAlmostEqual(time_value.real_return(0.07, 0.04), 0.028846, places=6)


class LoanTests(unittest.TestCase):
    def test_monthly_payment_known_value(self):
        # Classic textbook case: 200,000 at 6% for 30 years -> 1199.10/month.
        self.assertAlmostEqual(loans.monthly_payment(200_000, 0.06, 30), 1199.10, places=2)

    def test_zero_interest_loan(self):
        self.assertEqual(loans.monthly_payment(12_000, 0, 1), 1000)

    def test_schedule_ends_at_zero_and_principal_sums_to_loan(self):
        schedule = loans.amortization_schedule(10_000, 0.08, 3)
        self.assertEqual(len(schedule), 36)
        self.assertEqual(schedule[-1]["balance"], 0)
        self.assertAlmostEqual(sum(r["principal"] for r in schedule), 10_000, places=2)

    def test_interest_share_shrinks_over_time(self):
        schedule = loans.amortization_schedule(100_000, 0.09, 10)
        self.assertGreater(schedule[0]["interest"], schedule[-1]["interest"])
        self.assertLess(schedule[0]["principal"], schedule[-1]["principal"])

    def test_extra_payment_shortens_loan_and_saves_interest(self):
        base_interest = loans.total_interest(100_000, 0.09, 10)
        months, interest = loans.payoff_with_extra(100_000, 0.09, 10, 500)
        self.assertLess(months, 120)
        self.assertLess(interest, base_interest)

    def test_no_extra_matches_schedule(self):
        # 10,000 at 8% has a payment that rounds *down*, leaving a few cents
        # for the last month -- the term must still be exactly 36 months.
        months, interest = loans.payoff_with_extra(10_000, 0.08, 3, 0)
        self.assertEqual(months, 36)
        self.assertEqual(interest, loans.total_interest(10_000, 0.08, 3))

    def test_every_schedule_lasts_exactly_its_term(self):
        # Sweep many loans so rounding drift in either direction is covered.
        for amount in (5_000, 10_000, 123_457, 2_000_000):
            for rate in (0.0, 0.035, 0.08, 0.125):
                for years in (1, 3, 15, 30):
                    schedule = loans.amortization_schedule(amount, rate, years)
                    self.assertEqual(len(schedule), years * 12)
                    self.assertEqual(schedule[-1]["balance"], 0)
                    self.assertAlmostEqual(
                        sum(r["principal"] for r in schedule), amount, places=2
                    )


class InvestingTests(unittest.TestCase):
    def test_npv_known_value(self):
        flows = [-1000, 500, 500, 500]
        # -1000 + 500/1.1 + 500/1.21 + 500/1.331 = 243.43
        self.assertAlmostEqual(investing.npv(0.10, flows), 243.43, places=2)

    def test_irr_makes_npv_zero(self):
        flows = [-1000, 500, 500, 500]
        rate = investing.irr(flows)
        self.assertAlmostEqual(rate, 0.2338, places=4)
        self.assertAlmostEqual(investing.npv(rate, flows), 0, places=5)

    def test_irr_requires_mixed_signs(self):
        with self.assertRaises(ValueError):
            investing.irr([100, 100, 100])

    def test_cagr(self):
        self.assertAlmostEqual(investing.cagr(100, 200, 5), 0.1487, places=4)

    def test_volatility_of_constant_returns_is_zero(self):
        # assertAlmostEqual: floating-point sums can leave ~1e-18 of noise.
        self.assertAlmostEqual(investing.volatility([0.01] * 12), 0)

    def test_volatility_needs_two_points(self):
        with self.assertRaises(ValueError):
            investing.volatility([0.01])

    def test_sharpe_ratio(self):
        returns = [0.02, -0.01, 0.03, 0.00]
        # mean 0.01/month -> 0.12/yr; sample std ~0.01826 -> ~0.06325/yr
        self.assertAlmostEqual(investing.sharpe_ratio(returns, 0.0), 1.897, places=3)

    def test_max_drawdown(self):
        # Peak 120, trough 90 -> -25%. The later recovery doesn't erase it.
        self.assertAlmostEqual(investing.max_drawdown([100, 120, 90, 110, 130]), -0.25)

    def test_max_drawdown_rising_market_is_zero(self):
        self.assertEqual(investing.max_drawdown([1, 2, 3, 4]), 0)


if __name__ == "__main__":
    unittest.main()
