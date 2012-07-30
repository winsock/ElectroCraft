package info.cerios.electrocraft.core.utils;

public class ObjectTriplet<T1, T2, T3> {
	private T1 object1;
	private T2 object2;
	private T3 object3;
	
	public ObjectTriplet(T1 obj1, T2 obj2, T3 obj3) {
		this.object1 = obj1;
		this.object2 = obj2;
		this.object3 = obj3;
	}
	
	public T1 getValue1()
    {
        return this.object1;
    }

    public T2 getValue2()
    {
        return this.object2;
    }
    
    public T3 getValue3() {
    	return this.object3;
    }

    public void setValue1(T1 value)
    {
    	this.object1 = value;
    }

    public void setValue2(T2 value)
    {
    	this.object2 = value;
    }
    
    public void setValue3(T3 value) {
    	this.object3 = value;
    }
    
    @Override
    public int hashCode() {
    	return getValue1().hashCode() + getValue2().hashCode() + getValue3().hashCode();
    }
    
    @Override
    public boolean equals(Object anotherObject) {
    	if (!(anotherObject instanceof ObjectTriplet)) {
    		return false;
    	}
    	
    	if (!((ObjectTriplet) anotherObject).getValue1().equals(getValue1())){
    		return false;
    	}
    	
    	if (!((ObjectTriplet) anotherObject).getValue2().equals(getValue2())){
    		return false;
    	}
    	
    	if (!((ObjectTriplet) anotherObject).getValue3().equals(getValue3())){
    		return false;
    	}
    	
    	return true;
    }
}
