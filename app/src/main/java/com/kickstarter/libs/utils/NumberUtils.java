package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.NumberOptions;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public final class NumberUtils {
  private NumberUtils() {}

  public static @Nullable String numberWithDelimiter(final @Nullable Integer integer) {
    if (integer != null) {
      return NumberFormat.getNumberInstance().format(integer);
    }
    return null;
  }

  /**
   * Returns a formatted number for the user's locale. Defaults to 0 precision with no bucketing.
   */
  public static @NonNull String formatNumber(final float value) {
    return formatNumber(value, NumberOptions.builder().build());
  }

  /**
   * Returns a formatted number for the user's locale. {@link NumberOptions} can control whether the number is
   * used as a currency, if it is bucketed, and the precision.
   */
  public static @NonNull String formatNumber(final float value, final @NonNull NumberOptions options) {
    return formatNumber(value, options, Locale.getDefault());
  }

  /**
   * Returns a formatted number for a given locale. {@link NumberOptions} can control whether the number is
   * used as a currency, if it is bucketed, and the precision.
   */
  public static @NonNull String formatNumber(final float value, final @NonNull NumberOptions options,
    final @NonNull Locale locale) {

    final NumberFormat numberFormat = numberFormat(options, locale);
    if (numberFormat instanceof DecimalFormat) {
      numberFormat.setRoundingMode(RoundingMode.HALF_DOWN);
    }

    int precision = ObjectUtils.coalesce(options.precision(), 0);
    float divisor = 1.0f;
    String suffix = "";
    final float bucketAbove = ObjectUtils.coalesce(options.bucketAbove(), 0.0f);

    if (bucketAbove >= 1000.0f && value >= bucketAbove) {
      if (bucketAbove > 0.0f && bucketAbove < 1_000_000.0f) {
        divisor = 1000.0f;
        suffix = "K";
      } else if (bucketAbove >= 1_000_000.0f) {
        divisor = 1_000_000.0f;
        suffix = "M";
      }
      if (options.bucketAbove() != null) {
        precision = ObjectUtils.coalesce(options.bucketPrecision(), 0);
      }
    }

    if (options.currencyCode() != null) {
      suffix = String.format("%s %s", suffix, options.currencyCode());
    }

    numberFormat.setMinimumFractionDigits(precision);
    numberFormat.setMaximumFractionDigits(precision);

    float bucketedValue = value;
    if (value >= bucketAbove) {
      bucketedValue = value / divisor;
    }

    return String.format("%s%s", numberFormat.format(bucketedValue), suffix).trim();
  }

  /**
   * Return a formatter that can output an appropriate number based on the input currency and locale.
   */
  private static @NonNull NumberFormat numberFormat(final @NonNull NumberOptions options, final @NonNull Locale locale) {
    NumberFormat numberFormat;

    if (options.isCurrency()) {
      DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
      final DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
      symbols.setCurrencySymbol(options.currencySymbol());
      decimalFormat.setDecimalFormatSymbols(symbols);
      numberFormat = decimalFormat;
    } else {
      numberFormat = NumberFormat.getInstance(locale);
    }

    return numberFormat;
  }
}
