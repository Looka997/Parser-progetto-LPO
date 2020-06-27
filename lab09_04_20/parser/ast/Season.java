package lab09_04_20.parser.ast;

public enum Season {
    WINTER, SPRING, SUMMER, FALL;

    public static Season parseSeason(String season) {
        switch (season) {
            case "Winter":
                return WINTER;
            case "Spring":
                return SPRING;
            case "Summer":
                return SUMMER;
            case "Fall":
                return FALL;
            default:
                return null;
        }
    }
}
