---
layout: post
title:  "Java 8 functional interface 함수형 인터페이스"
date:   2018-07-06 02:02:00 +0900
categories: 
---

> [상위글: java8 문법 정리]({{ site.url }}{{ site.baseurl }}/post/2018-07-04-java8-syntax.html)

An informative annotation type used to indicate that an interface type declaration is intended to be a functional interface as defined by the Java Language Specification. Conceptually, a functional interface has exactly one abstract method. Since default methods have an implementation, they are not abstract. If an interface declares an abstract method overriding one of the public methods of java.lang.Object, that also does not count toward the interface's abstract method count since any implementation of the interface will have an implementation from java.lang.Object or elsewhere. 
Note that instances of functional interfaces can be created with lambda expressions, method references, or constructor references. 


## 함수형 인터페이스 (functional interface)

* 함수형 인터페이스는 한개의 메서드만을 갖는 인터페이스이다.

* 함수형 인터페이스 인스턴스는 람다식, 메서드 참조, 생성자 참조에 쓰일 수 있다.

## 함수형 인터페이스 선언하기

* 인터페이스 내에 메서드를 1개만 선언하여 만들 수 있다.

```java
interface Adder {
    int add(int a, int b);
}
```

* FunctionalInterface 어노테이션을 지정하여 선언할 수 있다.
* 어노테이션을 지정하게 되면 대상 타입이 인터페이스가 아니거나, 메서드가 2개이상일 경우 컴파일 에러를 발생시켜준다.
* 어노테이션을 지정하지 않더라도 위 조건을 만족한다면, 함수형 인터페이스 취급을 한다.

```java
@FunctionalInterface
interface Adder {
    int add(int a, int b);
}

@FunctionalInterface
interface Multiplier { // 메서드가 2개이므로 오류
    int multiply(int a, int b);

    int multiplyAndAdd(int a, int b);
}

@FunctionalInterface
class Calculator { // 클래스이므로 오류
    int multiply(int a, int b) {
        return a * b;
    }
}
```

* 인터페이스 내의 메서드가 Object 클래스의 메서드와 이름, 파라미터가 같다면 개수에 포함하지 않는다.
* 인터페이스 내의 default 메서드도 개수에 포함되지 않는다.

```java
@FunctionalInterface
interface Multiplier2 { // 컴파일 정상
    int multiply(int a, int b);

    String toString(); // Object의 메서드이므로 개수에 포함하지 않음
}
```

## 기본으로 제공되는 함수형 인터페이스

* 메서드 기능의 분류에 따라 기본적인 함수형 인터페이스들이 선언되어 있다.
  * Consumer: void accept(T t);
  * Predicate: boolean test(T t);
  * Function: R apply(T t);
  * Supplier: T get();
