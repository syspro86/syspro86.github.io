---
layout: post
title:  "Java 8 interface default method"
date:   2018-07-04 22:36:00 +0900
categories: 
---

> [상위글: java8 문법 정리]({{ site.url }}{{ site.baseurl }}/post/2018-07-04-java8-syntax.html)

## interface default method

* interface 메서드의 기본 구현을 작성할 수 있다.
* 해당 interface를 구현하는 클래스에서는 default 메서드를 override하거나 기본 구현을 그대로 사용할 수 있다.

### default 메서드 정의 문법

* 메서드 정의 앞부분에 default 키워드를 붙이고 메서드 구현을 작성한다.

{% highlight java %}
interface Adder {
    default int add(int a, int b) {
        return a + b;
    }
}
{% endhighlight %}

* default 를 붙이면 메서드 구현을 생략할 수 없다.

{% highlight java %}
interface Adder {
    default int add(int a, int b); // 구현 생략 불가
}
{% endhighlight %}

* interface를 구현하는 클래스에서는 default 메서드의 구현을 생략할 수 있다. 또는 override 하여 구현할 수 있다.

{% highlight java %}
class SubAdder implements Adder { // 에러 없음
}
{% endhighlight %}

{% highlight java %}
class SubAdder implements Adder {
    @Override
    public int add(int a, int b) { // override 구현 가능
        return a + b;
    }
}
{% endhighlight %}

* 정상/에러 코드

{% highlight java %}
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
{% endhighlight %}

