# SearchView
仿bilibili搜索框效果(只需要三句话即可实现)

先看预览图(转换后有一点点失真):

<img src="https://github.com/zhuowr2006/SearchView/blob/master/preview/2.png" width="30%" height="30%">

前言
-------
1,支持搜索历史(已经做了数据库存储了)
2,支持常用标签栏和最近输入标签栏
 
 
How to use
--------
第一句 , 实例化:
```java
  String[] mVals = new String[]
                {"Hello", "Android", "Weclome Hi ", "Button", "TextView", "Hello",
                        "Android", "Weclome", "Button ImageView", "TextView", "Helloworld",
                        "Android", "Weclome Hello", "Button Text", "TextView"};
        searchFragment = SearchFragment.newInstance(Arrays.asList(mVals),true);
```
第二句 , 设置回调:
```java
 searchFragment.setOnSearchClickListener(new IOnSearchClickListener() {
            @Override
            public void OnSearchClick(String keyword) {
                //这里处理逻辑
                Toast.makeText(ToolBarActivity.this, keyword, Toast.LENGTH_SHORT).show();
            }
        });
```
第三句 , 显示搜索框:
```java
  searchFragment.show(getSupportFragmentManager(),SearchFragment.TAG);
```
 
