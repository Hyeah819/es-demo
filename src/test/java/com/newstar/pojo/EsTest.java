package com.newstar.pojo;


import com.newstar.repository.ItemRepository;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import sun.reflect.generics.tree.VoidDescriptor;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsTest {
    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemRepository repository;
    @Test
    public void createtest(){
        //创建索引库
        elasticsearchTemplate.createIndex(Item.class);
        //创建映射
        elasticsearchTemplate.putMapping(Item.class);
    }
    @Test
    public void insertItem(){
        ArrayList<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", 3299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(3L, "华为META10", "手机", "华为", 4499.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", 2799.00, "http://image.leyou.com/13123.jpg"));
        // 接收对象集合，实现批量新增
        repository.saveAll(list);
    }
    @Test
    public void findItem(){
        Iterable<Item> all = repository.findAll();
        for (Item item : all) {
            System.out.println("=========="+item);
        }
    }
    @Test
    public void testFindByPrice(){
        List<Item> byPriceBetween = repository.findByPriceBetween(2000d, 4000d);
        for (Item item : byPriceBetween) {
            System.out.println("============="+item);
        }
    }
    @Test
    public void testQuery(){
        //创建查询构建起
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","price"},null));
        //添加查询条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("title","小米手机"));
        //排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        //分页(页码从0开始）
        queryBuilder.withPageable(PageRequest.of(0,2));
        //build()方法就是创建该对象吧
        Page<Item> search = repository.search(queryBuilder.build());

        long totalElements = search.getTotalElements();
        System.out.println("totalElements"+totalElements);
        int totalPages = search.getTotalPages();
        System.out.println("totalPages"+totalPages);
        List<Item> content = search.getContent();
        for (Item item : content) {
            System.out.println("item"+item);
        }
    }
    @Test
    public void testAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        String aggName = "popularBrand";
        //聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(aggName).field("brand"));
        //查询并返回带聚合结果
        AggregatedPage<Item> result = elasticsearchTemplate.queryForPage(queryBuilder.build(), Item.class);

        //解析聚合
        Aggregations aggregations = result.getAggregations();
        //获取指定名称的聚合
        StringTerms aggregation = aggregations.get(aggName);
        //获取桶
        List<StringTerms.Bucket> buckets = aggregation.getBuckets();
        for (StringTerms.Bucket bucket : buckets) {
            System.out.println("KEY="+bucket.getKeyAsString());
            System.out.println("docCount = "+bucket.getDocCount());
        }
    }

}
