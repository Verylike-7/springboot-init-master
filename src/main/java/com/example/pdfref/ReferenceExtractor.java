package com.example.pdfref;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReferenceExtractor {
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("《([^》]+)》\\s*[（(]\\s*([^）)]+?)\\s*[）)]");
    private static final Pattern ARABIC_REFERENCE_HEADING =
            Pattern.compile("^(\\d+(?:\\.\\d+)*)(?:[.．、])?\\s*(?:规范性)?引用文件$");
    private static final Pattern CHINESE_REFERENCE_HEADING =
            Pattern.compile("^(?:第)?([一二三四五六七八九十百千万]+)(?:[章节、.．])\\s*(?:规范性)?引用文件$");
    private static final Pattern BARE_REFERENCE_HEADING = Pattern.compile("^(?:规范性)?引用文件$");
    private static final Pattern ARABIC_HEADING = Pattern.compile("^(\\d+(?:\\.\\d+)*)(?:[.．、])?\\s+\\S.{0,60}$");
    private static final Pattern CHINESE_HEADING =
            Pattern.compile("^(?:第)?[一二三四五六七八九十百千万]+(?:[章节、.．])\\s*\\S.{0,60}$");

    private ReferenceExtractor() {
    }

    public static List<ReferenceItem> extract(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> lines = text.lines().map(String::trim).toList();
        Optional<Heading> heading = findReferenceHeading(lines);
        if (heading.isEmpty()) {
            return List.of();
        }

        String section = collectSection(lines, heading.get());
        return extractReferences(section);
    }

    private static Optional<Heading> findReferenceHeading(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = normalizeSpaces(lines.get(i));
            if (line.isEmpty()) {
                continue;
            }

            Matcher arabic = ARABIC_REFERENCE_HEADING.matcher(line);
            if (arabic.matches()) {
                return Optional.of(new Heading(i, HeadingStyle.ARABIC, numericLevel(arabic.group(1))));
            }
            if (CHINESE_REFERENCE_HEADING.matcher(line).matches()) {
                return Optional.of(new Heading(i, HeadingStyle.CHINESE, 1));
            }
            if (BARE_REFERENCE_HEADING.matcher(line).matches()) {
                return Optional.of(new Heading(i, HeadingStyle.BARE, 1));
            }
        }
        return Optional.empty();
    }

    private static String collectSection(List<String> lines, Heading startHeading) {
        StringBuilder section = new StringBuilder();
        for (int i = startHeading.lineIndex() + 1; i < lines.size(); i++) {
            String line = normalizeSpaces(lines.get(i));
            if (line.isEmpty()) {
                continue;
            }
            if (isSectionEnd(line, startHeading)) {
                break;
            }
            section.append(line).append('\n');
        }
        return section.toString();
    }

    private static boolean isSectionEnd(String line, Heading startHeading) {
        if (line.contains("《")) {
            return false;
        }

        return switch (startHeading.style()) {
            case ARABIC -> isSameLevelArabicHeading(line, startHeading.level());
            case CHINESE -> CHINESE_HEADING.matcher(line).matches();
            case BARE -> isAnyHeading(line);
        };
    }

    private static boolean isAnyHeading(String line) {
        return ARABIC_HEADING.matcher(line).matches()
                || CHINESE_HEADING.matcher(line).matches()
                || isPlainHeading(line);
    }

    private static boolean isSameLevelArabicHeading(String line, int level) {
        Matcher matcher = ARABIC_HEADING.matcher(line);
        return matcher.matches() && numericLevel(matcher.group(1)) == level;
    }

    private static boolean isPlainHeading(String line) {
        return line.length() <= 30
                && !line.matches(".*[。；;，,：:].*")
                && line.matches("[\\p{IsHan}A-Za-z0-9（）()、\\s-]+");
    }

    private static List<ReferenceItem> extractReferences(String section) {
        List<ReferenceItem> references = new ArrayList<>();
        Matcher matcher = REFERENCE_PATTERN.matcher(section);
        while (matcher.find()) {
            references.add(new ReferenceItem(matcher.group(1).trim(), matcher.group(2).trim()));
        }
        return references;
    }

    private static int numericLevel(String number) {
        return number.split("\\.").length;
    }

    private static String normalizeSpaces(String line) {
        return line.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private enum HeadingStyle {
        ARABIC,
        CHINESE,
        BARE
    }

    private record Heading(int lineIndex, HeadingStyle style, int level) {
    }
}
