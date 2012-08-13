package info.cerios.electrocraft.core.utils;

public enum Orientations {
    YNeg, // 0
    YPos, // 1
    ZNeg, // 2
    ZPos, // 3
    XNeg, // 4
    XPos, // 5
    Unknown;

    public Orientations reverse() {
        switch (this) {
            case YPos:
                return Orientations.YNeg;
            case YNeg:
                return Orientations.YPos;
            case ZPos:
                return Orientations.ZNeg;
            case ZNeg:
                return Orientations.ZPos;
            case XPos:
                return Orientations.XNeg;
            case XNeg:
                return Orientations.XPos;
            default:
                return Orientations.Unknown;
        }
    }

    public Orientations rotateLeft() {
        switch (this) {
            case XPos:
                return ZPos;
            case ZNeg:
                return XPos;
            case XNeg:
                return ZNeg;
            case ZPos:
                return XNeg;
            default:
                return this;
        }
    }

    public static Orientations[] dirs() {
        return new Orientations[]{YNeg, YPos, ZNeg, ZPos, XNeg, XPos};
    }
}