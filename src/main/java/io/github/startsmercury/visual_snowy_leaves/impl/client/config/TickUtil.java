package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.math.Saturating;
import net.minecraft.SharedConstants;

public final class TickUtil {
    public static String format(int ticks, final boolean full) {
        record Helper(StringBuilder builder) {
            public void prefixed(final char prefix, final int value) {
                builder.append(prefix);
                if (value < 10) {
                    builder.append('0');
                }
                builder.append(value);
            }
        }

        var seconds = Integer.divideUnsigned(ticks, SharedConstants.TICKS_PER_SECOND);
        var minutes = Integer.divideUnsigned(seconds, VslConstants.SECONDS_PER_MINUTE);
        var hours = Integer.divideUnsigned(minutes, VslConstants.MINUTES_PER_HOUR);

        ticks %= SharedConstants.TICKS_PER_SECOND;
        seconds %= VslConstants.SECONDS_PER_MINUTE;
        minutes %= VslConstants.MINUTES_PER_HOUR;

        final var builder = new StringBuilder(12);
        final var helper = new Helper(builder);
        if (full || hours > 0) {
            builder.append(hours);
            helper.prefixed(':', minutes);
            helper.prefixed(':', seconds);
        } else if (minutes > 0) {
            builder.append(minutes);
            helper.prefixed(':', seconds);
        } else {
            builder.append(seconds);
        }
        if (full || ticks > 0) {
            helper.prefixed('+', ticks);
        }
        return builder.toString();
    }

    public static int parse(final String input) {
        final var parser = new Parser(input);
        parser.parse();
        return parser.ticks;
    }

    private static final class Parser {
        private final String input;
        private int currentCodePoint;
        private int cursor;
        private int separatorHourMinute = -1;
        private int separatorMinuteSecond = -1;
        private int separatorSecondTick = -1;
        private int ticks;

        private Parser(final String input) {
            this.input = input;
        }

        private void parse() {
            findSeparators();

            cursor++;
            nextComponent();
            if (cursor > 0) {
                onInvalid("Expected ascii digit");
            }

            collectTicks();
        }

        private void findSeparators() {
            nextComponent();
            if (cursor >= 0) {
                if (currentCodePoint == '+') {
                    separatorSecondTick = cursor;
                    return;
                } else if (currentCodePoint != ':') {
                    onInvalid("Expected ':' or '+'");
                }
            }
            separatorHourMinute = cursor++;

            nextComponent();
            if (cursor < 0) {
                cursor = separatorHourMinute;
                separatorHourMinute = -1;
            } else if (currentCodePoint == '+') {
                separatorSecondTick = cursor;
                return;
            } else if (currentCodePoint != ':') {
                onInvalid("Expected ':' or '+'");
            }
            separatorMinuteSecond = cursor++;

            nextComponent();
            separatorSecondTick = cursor;
            if (separatorSecondTick < 0) {
                cursor = separatorMinuteSecond;
            } else if (currentCodePoint != '+') {
                onInvalid("Expected '+'");
            }
        }

        private void collectTicks() {
            if (separatorHourMinute >= 0) {
                parseMultiplied(VslConstants.TICKS_PER_HOUR, 0, separatorHourMinute++);
            } else {
                separatorHourMinute = 0;
            }

            if (separatorMinuteSecond >= 0) {
                parseMultiplied(SharedConstants.TICKS_PER_MINUTE, separatorHourMinute, separatorMinuteSecond++);
            } else {
                separatorMinuteSecond = separatorHourMinute;
            }

            if (separatorSecondTick >= 0) {
                final var start = separatorSecondTick + 1;
                final var end = input.length();
                if (end > start) {
                    ticks = Saturating.uAdd(ticks, Integer.parseUnsignedInt(input, start, end, 10));
                }
            } else {
                separatorSecondTick = input.length();
            }

            parseMultiplied(SharedConstants.TICKS_PER_SECOND, separatorMinuteSecond, separatorSecondTick);
        }

        private void nextComponent() {
            if (cursor < 0) {
                return;
            }

            for (; cursor < input.length(); cursor++) {
                final var cp = input.codePointAt(cursor);

                if ('0' > cp || cp > '9') {
                    currentCodePoint = cp;

                    return;
                }
            }

            cursor = -1;
        }

        private void onInvalid(final String reason) {
            throw new TickParseException(input, reason, cursor);
        }

        private void parseMultiplied(final int multiplier, final int start, final int end) {
            if (start == end) {
                return;
            }
            ticks = Saturating.uFma(
                ticks,
                multiplier,
                Integer.parseUnsignedInt(input, start, end, 10)
            );
        }
    }
}