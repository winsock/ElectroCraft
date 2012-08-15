package info.cerios.electrocraft.core.utils;

public class ObjectPair<T1, T2> {
    private T1 object1;
    private T2 object2;

    public ObjectPair(T1 obj1, T2 obj2) {
        this.object1 = obj1;
        this.object2 = obj2;
    }

    public T1 getValue1() {
        return this.object1;
    }

    public T2 getValue2() {
        return this.object2;
    }

    public void setValue1(T1 value) {
        this.object1 = value;
    }

    public void setValue2(T2 value) {
        this.object2 = value;
    }

    @Override
    public int hashCode() {
        return getValue1().hashCode() + getValue2().hashCode();
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (!(anotherObject instanceof ObjectPair)) {
            return false;
        }

        if (!((ObjectPair) anotherObject).getValue1().equals(getValue1())) {
            return false;
        }

        if (!((ObjectPair) anotherObject).getValue2().equals(getValue2())) {
            return false;
        }

        return true;
    }
}
