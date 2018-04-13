package com.example.demogemfire.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.gemfire.mapping.annotation.Region;

@Region(value = "Person")
public class Person {

    public Person() {
    }

    @Id
    private  String name;

    private  int age;

    @PersistenceConstructor
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return String.format("%s is %d years old", getName(), getAge());
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
