package com.newstar.repository;

import com.newstar.pojo.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemRepository extends ElasticsearchRepository<Item,Long> {
    //先写返回值，之后才会有提示List<Item>
    List<Item> findByPriceBetween(Double begin,Double end);
}
