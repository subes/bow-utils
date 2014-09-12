package be.bow.util;

public interface Compactable {

    //Make the object more space efficient. Calling this
    //method on an object that is already compacted should have no effect

    void compact();

}
