package org.blip.ms;

import org.junit.Test;

import static org.awaitility.Awaitility.*;
import static org.fest.assertions.api.Assertions.*;

public class ObjectsTest {

    @Test
    public void json() {
        Dog d = new Dog();
        d.name = "sparky";
        String json = Objects.toJson(d);
        System.out.println(json);

        Dog dog = Objects.fromJson(json, Dog.class);
        assertThat(dog.name).isEqualTo("sparky");
        assertThat(dog.speak()).isEqualTo("bark!");
    }

    private static abstract class Animal {
        public String name;

        abstract String speak();
    }

    private static class Dog extends Animal {

        @Override
        String speak() {
            return "bark!";
        }
    }

    private static class Cat extends  Animal {
        @Override
        String speak() {
            return "meow!";
        }
    }
}
