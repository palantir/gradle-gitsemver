package com.palantir.semver;

import java.util.Comparator;

class SemanticVersionComparator implements Comparator<SemanticVersion> {

    private static enum Keyword {
        DEV("dev", 0), ALPHA("alpha", 1), BETA("beta", 2), RC("rc", 3);

        private final String word;
        private final int precedence;

        private Keyword(String word, int precedence) {
            this.word = word;
            this.precedence = precedence;
        }

        public String getWord() {
            return word;
        }

        public static boolean isKeyword(String identifier) {
            return getKeywordForString(identifier) != null;
        }

        public static int compareKeywords(String oneIdentifier,
                                          String twoIdentifier) {
            Keyword oneKeyword = getKeywordForString(oneIdentifier);
            Keyword twoKeyword = getKeywordForString(twoIdentifier);
            return compareInts(oneKeyword.precedence, twoKeyword.precedence);
        }

        private static Keyword getKeywordForString(String string) {
            for (Keyword keyword : values()) {
                if (keyword.getWord().equals(string)) {
                    return keyword;
                }
            }
            return null;
        }

    }

    /**
     * Implementation of the semver.org 2.0.0-rc.2 comparison rules
     */
    @Override
    public int compare(SemanticVersion first, SemanticVersion other) {
        if (first.getMajorVersion() != other.getMajorVersion()) {
            return compareInts(first.getMajorVersion(), other.getMajorVersion());
        } else if (first.getMinorVersion() != other.getMinorVersion()) {
            return compareInts(first.getMinorVersion(), other.getMinorVersion());
        } else if (first.getPatchVersion() != other.getPatchVersion()) {
            return compareInts(first.getPatchVersion(), other.getPatchVersion());
        } else if (!objectsEqual(
                first.getReleaseCandidate(),
                other.getReleaseCandidate())) {
            return compareRcValues(
                    first.getReleaseCandidate(),
                    other.getReleaseCandidate());
        }
        return 0;
    }

    private static int compareRcValues(String one, String two) {
        if (one != null && two == null) {
            return -1;
        } else if (one == null && two != null) {
            return 1;
        } else {
            return compareNonNullRcValues(one, two);
        }
    }

    private static int compareNonNullRcValues(String one, String two) {
        String[] oneIdentifiers = one.split("\\.");
        String[] twoIdentifiers = two.split("\\.");
        int oneLength = oneIdentifiers.length;
        int twoLength = twoIdentifiers.length;
        int minLength = Math.min(oneLength, twoLength);
        int comparisonResult = compareUpToMinLength(
                oneIdentifiers,
                twoIdentifiers,
                minLength);
        if (comparisonResult != 0) {
            return comparisonResult;
        } else if (oneLength != twoLength) {
            return compareInts(twoLength, oneLength);
        } else {
            return 0;
        }
    }

    private static int compareUpToMinLength(String[] oneIdentifiers,
                                            String[] twoIdentifiers,
                                            int minLength) {
        for (int i = 0; i < minLength; i++) {
            String oneIdentifier = oneIdentifiers[i];
            String twoIdentifier = twoIdentifiers[i];
            if (!oneIdentifier.equals(twoIdentifier)) {
                if (i == 0) {
                    return compareIdentifiersCheckingForKeywords(
                            oneIdentifier,
                            twoIdentifier);
                } else {
                    return compareIdentifiers(oneIdentifier, twoIdentifier);
                }
            }
        }
        return 0;
    }

    private static int compareIdentifiersCheckingForKeywords(String oneIdentifier,
                                                             String twoIdentifier) {
        boolean oneKeyword = Keyword.isKeyword(oneIdentifier);
        boolean twoKeyword = Keyword.isKeyword(twoIdentifier);
        if (oneKeyword && twoKeyword) {
            return Keyword.compareKeywords(oneIdentifier, twoIdentifier);
        } else if (!oneKeyword && !twoKeyword) {
            return compareIdentifiers(oneIdentifier, twoIdentifier);
        } else {
            return oneKeyword ? 1 : -1;
        }
    }

    private static int compareIdentifiers(String oneIdentifier,
                                          String twoIdentifier) {
        boolean oneNumeric = isNumeric(oneIdentifier);
        boolean twoNumeric = isNumeric(twoIdentifier);
        if (oneNumeric && twoNumeric) {
            int oneInt = Integer.parseInt(oneIdentifier);
            int twoInt = Integer.parseInt(twoIdentifier);
            return compareInts(oneInt, twoInt);
        } else if (!oneNumeric && !twoNumeric) {
            return oneIdentifier.compareTo(twoIdentifier);
        } else {
            return oneNumeric ? -1 : 1;
        }
    }

    private static boolean isNumeric(String oneIdentifier) {
        return oneIdentifier.matches("\\d+");
    }

    private static int compareInts(int a, int b) {
        return Integer.valueOf(a).compareTo(Integer.valueOf(b));
    }

    private static boolean objectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

}
