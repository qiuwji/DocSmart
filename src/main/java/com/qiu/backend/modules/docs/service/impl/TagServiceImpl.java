package com.qiu.backend.modules.docs.service.impl;


import com.qiu.backend.modules.docs.mapper.TagMapper;
import com.qiu.backend.modules.docs.service.TagService;
import com.qiu.backend.modules.model.entity.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Autowired
    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @Override
    public List<Long> getOrCreateTagIdsByName(List<String> tags, Long userId) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 去重、防止前端传来重复 tag
        Set<String> uniqueNames = new LinkedHashSet<>(tags);

        // 2. 查询已有的 Tag
        List<Tag> existTags = tagMapper.selectByNamesAndUserId(new ArrayList<>(uniqueNames), userId);

        // 3. 把已有的放到 map 中
        Map<String, Long> nameToId = new HashMap<>();
        for (Tag t : existTags) {
            nameToId.put(t.getName(), t.getId());
        }

        List<Long> resultIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 4. 遍历所有名称，已存在就取 id，否则插入新记录
        for (String name : uniqueNames) {
            if (nameToId.containsKey(name)) {
                resultIds.add(nameToId.get(name));
            } else {
                Tag newTag = new Tag();
                newTag.setName(name);
                newTag.setUserId(userId);
                newTag.setCreateTime(now);
                newTag.setUpdateTime(now);

                int rows = tagMapper.insert(newTag);
                if (rows > 0) {
                    resultIds.add(newTag.getId()); // useGeneratedKeys 已回填
                } else {
                    log.warn("插入 tag 失败: name={}, userId={}", name, userId);
                }
            }
        }

        return resultIds;
    }
}
