---
layout: default
permalink: /categories/
title: Categories
---
<div class="home">
  <h1 class="page-heading">Categories / <a href="..">Posts</a></h1>
  <ul class="post-list">
    {% for category in site.data.categories %}
    <div class="archive-group">
      <h2>
        <a href="{{ site.baseurl }}/categories/{{ category.path }}" class="category-head">{{ category.name | slugize }}</a>
      </h2>

      {% for post in site.categories[category.path] %}
      <li>
        <span class="post-meta">{{ post.date | date: "%Y/%m/%d" }}</span>
        <a class="post-link" href="{{ post.url | prepend: site.baseurl }}">{{ post.title }}</a>
      </li>
      {% if forloop.index >= 5 %}
      {%   break %}
      {% endif %}
      {% endfor %}
    </div>
    {% endfor %}
  </ul>
</div>
