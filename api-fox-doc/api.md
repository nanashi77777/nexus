# nexus_v1.main

exported at 2025-07-18 15:23:35

## 资源管理接口

资源管理接口


---

### 创建手动资源

> BASIC

**Path:** /api/v1/resources/manual

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name            | type    | desc   |
|-----------------|---------|--------|
| learningSpaceId | integer | 学习空间ID |
| title           | string  | 资源标题   |
| description     | string  | 资源描述   |
| sliceStrategy   | integer | 分片策略   |

**Request Demo:**

```json
{
  "learningSpaceId": 0,
  "title": "",
  "description": "",
  "sliceStrategy": "SliceStrategyEnum.RECURSIVE_BY_TOKEN.getCode()"
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type    | desc          |
|---------|---------|---------------|
| code    | string  | 状态码           |
| message | string  | 消息描述          |
| data    | integer | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": 0
}
```

---

### 上传资源文件

> BASIC

**Path:** /api/v1/resources/upload

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value               | required | desc |
|--------------|---------------------|----------|------|
| Content-Type | multipart/form-data | YES      |      |

**Request Body:**

| name            | type    | desc   |
|-----------------|---------|--------|
| learningSpaceId | integer | 学习空间ID |
| title           | string  | 资源标题   |
| description     | string  | 资源描述   |
| sliceStrategy   | integer | 分片策略   |

**Request Demo:**

```json
{
  "learningSpaceId": 0,
  "title": "",
  "description": "",
  "sliceStrategy": "SliceStrategyEnum.RECURSIVE_BY_TOKEN.getCode()"
}
```

**Form:**

| name | value | required | type | desc |
|------|-------|----------|------|------|
| file |       | YES      | file | 文件   |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                          | type    | desc          |
|-------------------------------|---------|---------------|
| code                          | string  | 状态码           |
| message                       | string  | 消息描述          |
| data                          | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─resourceId | integer |               |
| &ensp;&ensp;&#124;─taskId     | integer |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "resourceId": 0,
    "taskId": 0
  }
}
```

---

### 创建链接资源

> BASIC

**Path:** /api/v1/resources/link

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name            | type    | desc   |
|-----------------|---------|--------|
| learningSpaceId | integer | 学习空间ID |
| url             | string  | 资源链接   |
| title           | string  | 资源标题   |
| description     | string  | 资源描述   |
| sliceStrategy   | integer | 分片策略   |

**Request Demo:**

```json
{
  "learningSpaceId": 0,
  "url": "",
  "title": "",
  "description": "",
  "sliceStrategy": 0
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                          | type    | desc          |
|-------------------------------|---------|---------------|
| code                          | string  | 状态码           |
| message                       | string  | 消息描述          |
| data                          | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─resourceId | integer |               |
| &ensp;&ensp;&#124;─taskId     | integer |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "resourceId": 0,
    "taskId": 0
  }
}
```

---

### 创建AI搜索资源

> BASIC

**Path:** /api/v1/resources/ai-search

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name              | type    | desc |
|-------------------|---------|------|
| learningSpaceId   | integer |      |
| requirementPrompt | string  |      |
| title             | string  |      |
| description       | string  |      |

**Request Demo:**

```json
{
  "learningSpaceId": 0,
  "requirementPrompt": "",
  "title": "",
  "description": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                          | type    | desc          |
|-------------------------------|---------|---------------|
| code                          | string  | 状态码           |
| message                       | string  | 消息描述          |
| data                          | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─resourceId | integer |               |
| &ensp;&ensp;&#124;─taskId     | integer |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "resourceId": 0,
    "taskId": 0
  }
}
```

---

### 更新资源信息

> BASIC

**Path:** /api/v1/resources/{resourceId}

**Method:** PUT

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name       | value | desc |
|------------|-------|------|
| resourceId |       | 资源ID |

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name        | type   | desc |
|-------------|--------|------|
| title       | string |      |
| description | string |      |

**Request Demo:**

```json
{
  "title": "",
  "description": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 为资源创建新的分片

> BASIC

**Path:** /api/v1/resources/{resourceId}/chunks

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name       | value | desc |
|------------|-------|------|
| resourceId |       | 资源ID |

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name       | type    | desc |
|------------|---------|------|
| content    | string  |      |
| pageIndex  | integer |      |
| chunkIndex | integer |      |

**Request Demo:**

```json
{
  "content": "",
  "pageIndex": 0,
  "chunkIndex": 0
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type    | desc          |
|---------|---------|---------------|
| code    | string  | 状态码           |
| message | string  | 消息描述          |
| data    | integer | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": 0
}
```

---

### 更新资源块

> BASIC

**Path:** /api/v1/resources/chunks/{chunkId}

**Method:** PUT

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name    | value | desc  |
|---------|-------|-------|
| chunkId |       | 资源块ID |

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name    | type   | desc |
|---------|--------|------|
| content | string | 分片内容 |

**Request Demo:**

```json
{
  "content": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 触发向量化

> BASIC

**Path:** /api/v1/resources/chunks/{chunkId}/vectorize

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name    | value | desc |
|---------|-------|------|
| chunkId |       | 块ID  |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type    | desc          |
|---------|---------|---------------|
| code    | string  | 状态码           |
| message | string  | 消息描述          |
| data    | integer | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": 0
}
```

---

### 删除资源分片

> BASIC

**Path:** /api/v1/resources/chunks/{chunkId}

**Method:** DELETE

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name    | value | desc |
|---------|-------|------|
| chunkId |       | 块ID  |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 取消分片向量化

> BASIC

**Path:** /api/v1/resources/chunks/{chunkId}/vectorization

**Method:** DELETE

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name    | value | desc |
|---------|-------|------|
| chunkId |       | 块ID  |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 触发分片批量向量化

> BASIC

**Path:** /api/v1/resources/chunks/batch-vectorize

**Method:** POST

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name                | type    | desc |
|---------------------|---------|------|
| chunkIds            | array   |      |
| &ensp;&ensp;&#124;─ | integer |      |

**Request Demo:**

```json
{
  "chunkIds": [
    0
  ]
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type    | desc          |
|---------|---------|---------------|
| code    | string  | 状态码           |
| message | string  | 消息描述          |
| data    | integer | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": 0
}
```

---

### 批量取消分片向量化

> BASIC

**Path:** /api/v1/resources/chunks/batch-vectorization

**Method:** DELETE

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name                | type    | desc |
|---------------------|---------|------|
| chunkIds            | array   |      |
| &ensp;&ensp;&#124;─ | integer |      |

**Request Demo:**

```json
{
  "chunkIds": [
    0
  ]
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### getResourcesByPage

> BASIC

**Path:** /api/v1/resources

**Method:** GET

> REQUEST

**Query:**

| name            | value | required | desc                      |
|-----------------|-------|----------|---------------------------|
| pageNum         |       | NO       | 页码                        |
| pageSize        |       | NO       | 每页条数                      |
| keyword         |       | NO       | 关键词搜索<br>搜索关键词            |
| sortField       |       | NO       | 排序字段                      |
| sortDirection   |       | NO       | 排序方向：ASC升序、DESC降序<br>排序方向 |
| learningSpaceId |       | YES      | 学习空间ID                    |
| titleKeyword    |       | NO       | 资源标题关键字                   |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                                                       | type    | desc          |
|------------------------------------------------------------|---------|---------------|
| code                                                       | string  | 状态码           |
| message                                                    | string  | 消息描述          |
| data                                                       | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─records                                 | array   | 当前页数据列表       |
| &ensp;&ensp;&ensp;&ensp;&#124;─                            | object  |               |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─id              | integer |               |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─learningSpaceId | integer |               |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─title           | string  |               |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─description     | string  |               |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─sourceType      | integer |               |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─sourceUri       | string  |               |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─createdAt       | string  |               |
| &ensp;&ensp;&#124;─total                                   | integer | 总记录数          |
| &ensp;&ensp;&#124;─pageNum                                 | integer | 当前页码          |
| &ensp;&ensp;&#124;─pageSize                                | integer | 每页记录数         |
| &ensp;&ensp;&#124;─pages                                   | integer | 总页数           |
| &ensp;&ensp;&#124;─hasPrevious                             | boolean | 是否有上一页        |
| &ensp;&ensp;&#124;─hasNext                                 | boolean | 是否有下一页        |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "records": [
      {
        "id": 0,
        "learningSpaceId": 0,
        "title": "",
        "description": "",
        "sourceType": 0,
        "sourceUri": "",
        "createdAt": ""
      }
    ],
    "total": 0,
    "pageNum": 0,
    "pageSize": 0,
    "pages": 0,
    "hasPrevious": false,
    "hasNext": false
  }
}
```

---

### getResourceById

> BASIC

**Path:** /api/v1/resources/{resourceId}

**Method:** GET

> REQUEST

**Path Params:**

| name       | value | desc |
|------------|-------|------|
| resourceId |       |      |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                               | type    | desc          |
|------------------------------------|---------|---------------|
| code                               | string  | 状态码           |
| message                            | string  | 消息描述          |
| data                               | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─id              | integer |               |
| &ensp;&ensp;&#124;─learningSpaceId | integer |               |
| &ensp;&ensp;&#124;─title           | string  |               |
| &ensp;&ensp;&#124;─description     | string  |               |
| &ensp;&ensp;&#124;─sourceType      | integer |               |
| &ensp;&ensp;&#124;─sourceUri       | string  |               |
| &ensp;&ensp;&#124;─createdAt       | string  |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "id": 0,
    "learningSpaceId": 0,
    "title": "",
    "description": "",
    "sourceType": 0,
    "sourceUri": "",
    "createdAt": ""
  }
}
```

## LearningSpaceController

LearningSpaceController


---

### 创建学习空间

> BASIC

**Path:** /api/v1/isolation/learning-space

**Method:** POST

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name          | type   | desc             |
|---------------|--------|------------------|
| name          | string | 学习空间的名称          |
| description   | string | 对学习空间的详细描述       |
| spacePrompt   | string | 空间内AI的全局参考Prompt |
| coverImageUrl | string | 空间封面图URL         |

**Request Demo:**

```json
{
  "name": "",
  "description": "",
  "spacePrompt": "",
  "coverImageUrl": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type    | desc          |
|---------|---------|---------------|
| code    | string  | 状态码           |
| message | string  | 消息描述          |
| data    | integer | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": 0
}
```

---

### 更新学习空间

> BASIC

**Path:** /api/v1/isolation/learning-space/{id}

**Method:** PUT

> REQUEST

**Path Params:**

| name | value | desc   |
|------|-------|--------|
| id   |       | 学习空间ID |

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name          | type   | desc             |
|---------------|--------|------------------|
| name          | string | 学习空间的名称          |
| description   | string | 对学习空间的详细描述       |
| spacePrompt   | string | 空间内AI的全局参考Prompt |
| coverImageUrl | string | 空间封面图URL         |

**Request Demo:**

```json
{
  "name": "",
  "description": "",
  "spacePrompt": "",
  "coverImageUrl": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 删除学习空间

> BASIC

**Path:** /api/v1/isolation/learning-space/{id}

**Method:** DELETE

> REQUEST

**Path Params:**

| name | value | desc   |
|------|-------|--------|
| id   |       | 学习空间ID |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

## KnowledgeController

KnowledgeController


---

### 创建知识点

> BASIC

**Path:** /api/v1/knowledge/points

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name          | type    | desc |
|---------------|---------|------|
| folderId      | integer |      |
| title         | string  |      |
| definition    | string  |      |
| explanation   | string  |      |
| formulaOrCode | string  |      |
| example       | string  |      |
| difficulty    | number  |      |

**Request Demo:**

```json
{
  "folderId": 0,
  "title": "",
  "definition": "",
  "explanation": "",
  "formulaOrCode": "",
  "example": "",
  "difficulty": 0.0
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                                | type    | desc          |
|-------------------------------------|---------|---------------|
| code                                | string  | 状态码           |
| message                             | string  | 消息描述          |
| data                                | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─id               | integer |               |
| &ensp;&ensp;&#124;─folderId         | integer |               |
| &ensp;&ensp;&#124;─title            | string  |               |
| &ensp;&ensp;&#124;─definition       | string  |               |
| &ensp;&ensp;&#124;─explanation      | string  |               |
| &ensp;&ensp;&#124;─formulaOrCode    | string  |               |
| &ensp;&ensp;&#124;─example          | string  |               |
| &ensp;&ensp;&#124;─difficulty       | number  |               |
| &ensp;&ensp;&#124;─currentVersionId | integer |               |
| &ensp;&ensp;&#124;─createdAt        | string  |               |
| &ensp;&ensp;&#124;─updatedAt        | string  |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "id": 0,
    "folderId": 0,
    "title": "",
    "definition": "",
    "explanation": "",
    "formulaOrCode": "",
    "example": "",
    "difficulty": 0.0,
    "currentVersionId": 0,
    "createdAt": "",
    "updatedAt": ""
  }
}
```

---

### 更新知识点

> BASIC

**Path:** /api/v1/knowledge/points/{pointId}

**Method:** PUT

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name    | value | desc  |
|---------|-------|-------|
| pointId |       | 知识点ID |

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name            | type    | desc |
|-----------------|---------|------|
| title           | string  |      |
| definition      | string  |      |
| explanation     | string  |      |
| formulaOrCode   | string  |      |
| example         | string  |      |
| difficulty      | number  |      |
| isSaveAsVersion | boolean |      |

**Request Demo:**

```json
{
  "title": "",
  "definition": "",
  "explanation": "",
  "formulaOrCode": "",
  "example": "",
  "difficulty": 0.0,
  "isSaveAsVersion": "false"
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                                | type    | desc          |
|-------------------------------------|---------|---------------|
| code                                | string  | 状态码           |
| message                             | string  | 消息描述          |
| data                                | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─id               | integer |               |
| &ensp;&ensp;&#124;─folderId         | integer |               |
| &ensp;&ensp;&#124;─title            | string  |               |
| &ensp;&ensp;&#124;─definition       | string  |               |
| &ensp;&ensp;&#124;─explanation      | string  |               |
| &ensp;&ensp;&#124;─formulaOrCode    | string  |               |
| &ensp;&ensp;&#124;─example          | string  |               |
| &ensp;&ensp;&#124;─difficulty       | number  |               |
| &ensp;&ensp;&#124;─currentVersionId | integer |               |
| &ensp;&ensp;&#124;─createdAt        | string  |               |
| &ensp;&ensp;&#124;─updatedAt        | string  |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "id": 0,
    "folderId": 0,
    "title": "",
    "definition": "",
    "explanation": "",
    "formulaOrCode": "",
    "example": "",
    "difficulty": 0.0,
    "currentVersionId": 0,
    "createdAt": "",
    "updatedAt": ""
  }
}
```

---

### 删除知识点

> BASIC

**Path:** /api/v1/knowledge/points/{pointId}

**Method:** DELETE

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name    | value | desc  |
|---------|-------|-------|
| pointId |       | 知识点ID |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 创建知识点关系

> BASIC

**Path:** /api/v1/knowledge/relations

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name            | type    | desc |
|-----------------|---------|------|
| learningSpaceId | integer |      |
| sourcePointId   | integer |      |
| targetPointId   | integer |      |
| relationType    | string  |      |
| description     | string  |      |

**Request Demo:**

```json
{
  "learningSpaceId": 0,
  "sourcePointId": 0,
  "targetPointId": 0,
  "relationType": "",
  "description": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                             | type    | desc          |
|----------------------------------|---------|---------------|
| code                             | string  | 状态码           |
| message                          | string  | 消息描述          |
| data                             | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─id            | integer |               |
| &ensp;&ensp;&#124;─sourcePointId | integer |               |
| &ensp;&ensp;&#124;─targetPointId | integer |               |
| &ensp;&ensp;&#124;─relationType  | string  |               |
| &ensp;&ensp;&#124;─description   | string  |               |
| &ensp;&ensp;&#124;─createdAt     | string  |               |
| &ensp;&ensp;&#124;─updatedAt     | string  |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "id": 0,
    "sourcePointId": 0,
    "targetPointId": 0,
    "relationType": "",
    "description": "",
    "createdAt": "",
    "updatedAt": ""
  }
}
```

---

### 更新知识点关系

> BASIC

**Path:** /api/v1/knowledge/relations/{relationId}

**Method:** PUT

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name       | value | desc |
|------------|-------|------|
| relationId |       | 关系ID |

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name         | type   | desc |
|--------------|--------|------|
| relationType | string |      |
| description  | string |      |

**Request Demo:**

```json
{
  "relationType": "",
  "description": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                             | type    | desc          |
|----------------------------------|---------|---------------|
| code                             | string  | 状态码           |
| message                          | string  | 消息描述          |
| data                             | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─id            | integer |               |
| &ensp;&ensp;&#124;─sourcePointId | integer |               |
| &ensp;&ensp;&#124;─targetPointId | integer |               |
| &ensp;&ensp;&#124;─relationType  | string  |               |
| &ensp;&ensp;&#124;─description   | string  |               |
| &ensp;&ensp;&#124;─createdAt     | string  |               |
| &ensp;&ensp;&#124;─updatedAt     | string  |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "id": 0,
    "sourcePointId": 0,
    "targetPointId": 0,
    "relationType": "",
    "description": "",
    "createdAt": "",
    "updatedAt": ""
  }
}
```

---

### 删除知识点关系

> BASIC

**Path:** /api/v1/knowledge/relations/{relationId}

**Method:** DELETE

**Desc:**

COMPLETED

> REQUEST

**Path Params:**

| name       | value | desc |
|------------|-------|------|
| relationId |       | 关系ID |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

## UserController

UserController


---

### 用户注册

> BASIC

**Path:** /api/v1/user/register

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name             | type   | desc |
|------------------|--------|------|
| username         | string |      |
| email            | string |      |
| password         | string |      |
| inviteCode       | string |      |
| verificationCode | string |      |

**Request Demo:**

```json
{
  "username": "",
  "email": "",
  "password": "",
  "inviteCode": "",
  "verificationCode": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type    | desc          |
|---------|---------|---------------|
| code    | string  | 状态码           |
| message | string  | 消息描述          |
| data    | integer | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": 0
}
```

---

### 发送验证码

> BASIC

**Path:** /api/v1/user/send-register-code

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name  | type   | desc |
|-------|--------|------|
| email | string |      |

**Request Demo:**

```json
{
  "email": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 修改密码

> BASIC

**Path:** /api/v1/user/change-password

**Method:** PUT

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name        | type   | desc |
|-------------|--------|------|
| oldPassword | string |      |
| newPassword | string |      |

**Request Demo:**

```json
{
  "oldPassword": "",
  "newPassword": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | string | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": ""
}
```

---

### 忘记密码

> BASIC

**Path:** /api/v1/user/forgot-password

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name  | type   | desc |
|-------|--------|------|
| email | string | 用户邮箱 |

**Request Demo:**

```json
{
  "email": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | string | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": ""
}
```

---

### 重置密码

> BASIC

**Path:** /api/v1/user/reset-password

**Method:** PUT

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name             | type   | desc |
|------------------|--------|------|
| email            | string | 用户邮箱 |
| verificationCode | string | 验证码  |
| newPassword      | string | 新密码  |

**Request Demo:**

```json
{
  "email": "",
  "verificationCode": "",
  "newPassword": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | string | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": ""
}
```

---

### 用户登录

> BASIC

**Path:** /api/v1/user/login

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name     | type   | desc   |
|----------|--------|--------|
| account  | string | 用户名或邮箱 |
| password | string | 密码     |

**Request Demo:**

```json
{
  "account": "",
  "password": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                          | type    | desc          |
|-------------------------------|---------|---------------|
| code                          | string  | 状态码           |
| message                       | string  | 消息描述          |
| data                          | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─userId     | integer | 用户ID          |
| &ensp;&ensp;&#124;─username   | string  | 用户名           |
| &ensp;&ensp;&#124;─tokenName  | string  | 令牌名称          |
| &ensp;&ensp;&#124;─tokenValue | string  | 令牌值           |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "userId": 0,
    "username": "",
    "tokenName": "",
    "tokenValue": ""
  }
}
```

---

### 用户退出登录

> BASIC

**Path:** /api/v1/user/logout

**Method:** POST

**Desc:**

COMPLETED

> REQUEST



> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 用户账号注销

> BASIC

**Path:** /api/v1/user/deactivate

**Method:** DELETE

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name             | type   | desc           |
|------------------|--------|----------------|
| password         | string |                |
| deactivateReason | string | TODO：后续将原因进行记录 |

**Request Demo:**

```json
{
  "password": "",
  "deactivateReason": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | string | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": ""
}
```

---

### 发送邮箱更新验证码

> BASIC

**Path:** /api/v1/user/send-update-email-code

**Method:** POST

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name  | type   | desc |
|-------|--------|------|
| email | string |      |

**Request Demo:**

```json
{
  "email": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | string | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": ""
}
```

---

### 修改用户邮箱

> BASIC

**Path:** /api/v1/user/update-email

**Method:** PUT

**Desc:**

COMPLETED

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name             | type   | desc |
|------------------|--------|------|
| newEmail         | string |      |
| verificationCode | string |      |

**Request Demo:**

```json
{
  "newEmail": "",
  "verificationCode": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | string | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": ""
}
```

## KnowledgeFolderController

KnowledgeFolderController


---

### 创建文件夹

> BASIC

**Path:** /api/v1/knowledge/folders

**Method:** POST

**Desc:**

COMPLETE

> REQUEST

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name            | type    | desc |
|-----------------|---------|------|
| learningSpaceId | integer |      |
| parentId        | integer |      |
| name            | string  |      |

**Request Demo:**

```json
{
  "learningSpaceId": 0,
  "parentId": 0,
  "name": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name                         | type    | desc          |
|------------------------------|---------|---------------|
| code                         | string  | 状态码           |
| message                      | string  | 消息描述          |
| data                         | object  | 数据，可以是任何类型的VO |
| &ensp;&ensp;&#124;─id        | integer |               |
| &ensp;&ensp;&#124;─parentId  | integer |               |
| &ensp;&ensp;&#124;─name      | string  |               |
| &ensp;&ensp;&#124;─createdAt | string  |               |
| &ensp;&ensp;&#124;─updatedAt | string  |               |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": {
    "id": 0,
    "parentId": 0,
    "name": "",
    "createdAt": "",
    "updatedAt": ""
  }
}
```

---

### 重命名文件夹

> BASIC

**Path:** /api/v1/knowledge/folders/{folderId}/name

**Method:** PUT

**Desc:**

COMPLETE

> REQUEST

**Path Params:**

| name     | value | desc  |
|----------|-------|-------|
| folderId |       | 文件夹ID |

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name    | type   | desc |
|---------|--------|------|
| newName | string |      |

**Request Demo:**

```json
{
  "newName": ""
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 移动文件夹

> BASIC

**Path:** /api/v1/knowledge/folders/{folderId}/move

**Method:** PUT

**Desc:**

COMPLETE

> REQUEST

**Path Params:**

| name     | value | desc  |
|----------|-------|-------|
| folderId |       | 文件夹ID |

**Headers:**

| name         | value            | required | desc |
|--------------|------------------|----------|------|
| Content-Type | application/json | YES      |      |

**Request Body:**

| name              | type    | desc |
|-------------------|---------|------|
| newParentFolderId | integer |      |

**Request Demo:**

```json
{
  "newParentFolderId": 0
}
```

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```

---

### 删除文件夹

> BASIC

**Path:** /api/v1/knowledge/folders/{folderId}

**Method:** DELETE

**Desc:**

TODO: 删除文件夹下的所有文件和子文件夹

> REQUEST

**Path Params:**

| name     | value | desc  |
|----------|-------|-------|
| folderId |       | 文件夹ID |

> RESPONSE

**Headers:**

| name         | value                          | required | desc |
|--------------|--------------------------------|----------|------|
| content-type | application/json;charset=UTF-8 | NO       |      |

**Body:**

| name    | type   | desc          |
|---------|--------|---------------|
| code    | string | 状态码           |
| message | string | 消息描述          |
| data    | object | 数据，可以是任何类型的VO |

**Response Demo:**

```json
{
  "code": "",
  "message": "",
  "data": null
}
```





