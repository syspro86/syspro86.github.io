---
layout: post
title:  "Java 8 문법 정리"
date:   2018-07-04 22:36:00 +0900
categories: java
---

http://openjdk.java.net/projects/jdk8/features

## 인터페이스 default 메서드

* interface 메서드의 기본 구현을 작성할 수 있다.
* 해당 interface를 구현하는 클래스에서는 default 메서드를 override하거나 기본 구현을 그대로 사용할 수 있다.

### default 메서드 정의 문법

* 메서드 정의 앞부분에 default 키워드를 붙이고 메서드 구현을 작성한다.

```java
interface Adder {
    default int add(int a, int b) {
        return a + b;
    }
}
```

* default 를 붙이면 메서드 구현을 생략할 수 없다.

```java
interface Adder {
    default int add(int a, int b); // 구현 생략 불가
}
```

* interface를 구현하는 클래스에서는 default 메서드의 구현을 생략할 수 있다. 또는 override 하여 구현할 수 있다.

```java
class SubAdder implements Adder { // 에러 없음
}
```

```java
class SubAdder implements Adder {
    @Override
    public int add(int a, int b) { // override 구현 가능
        return a + b;
    }
}
```

* 정상/에러 코드

```java
package net.zsoo.java8.sample;

public class InterfaceDefaultMethod_Adder {
    public static void main(String[] args) {
        System.out.println(new SubAdder().add(1, 2)); // 정상. 미구현 method 없음
        System.out.println(new Adder().add(1, 2)); // 에러. Adder에 미구현 method가 없더라도 Adder는 interface이기 때문에 new 불가
        System.out.println(new Adder() {}.add(1, 2)); // 정상. new Adder() {} 형태로 무명 클래스가 정의되어 생성 가능
    }

    interface Adder {
        default int add(int a, int b) {
            return a + b;
        }
    }
    
    class SubAdder implements Adder {
        @Override
        public int add(int a, int b) {
            return a + b;
        }
    }
}
```


## 함수형 인터페이스 (functional interface)

* 함수형 인터페이스는 한개의 메서드만을 갖는 인터페이스이다.

* 함수형 인터페이스 인스턴스는 람다식, 메서드 참조, 생성자 참조에 쓰일 수 있다.

### 함수형 인터페이스 선언하기

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

### 기본으로 제공되는 함수형 인터페이스

* 메서드 기능의 분류에 따라 기본적인 함수형 인터페이스들이 선언되어 있다.
  * Consumer: void accept(T t);
  * Predicate: boolean test(T t);
  * Function: R apply(T t);
  * Supplier: T get();



## [람다식 lambda expression]({{ site.url }}{{ site.baseurl }}/post/2018-07-06-java8-lambda-expression.html)

## Lambda 람다식

Consumer
Predicate
Function
Supplier

## Method Reference

ClassName::methodName
