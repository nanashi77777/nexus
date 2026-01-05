import request from '@/utils/request'

export interface KnowledgePoint {
  id: number
  title: string
  definition?: string
  explanation?: string
  formulaOrCode?: string
  example?: string
  createdAt: string
  updatedAt: string
}

export interface KnowledgePointRelation {
  id: number
  sourcePointId: number
  targetPointId: number
  relationType: string
  description?: string
  createdAt: string
  updatedAt: string
}

export interface KnowledgeGraphData {
  points: KnowledgePoint[]
  relations: KnowledgePointRelation[]
}

/**
 * 获取用户的所有知识点
 */
export const getKnowledgePoints = (): Promise<KnowledgePoint[]> => {
  return request.get('/api/v1/knowledge/points')
}

/**
 * 获取知识点关系
 */
export const getKnowledgePointRelations = (): Promise<KnowledgePointRelation[]> => {
  return request.get('/api/v1/knowledge/relations')
}

/**
 * 获取完整的知识图谱数据
 */
export const getKnowledgeGraph = async (): Promise<KnowledgeGraphData> => {
  const [points, relations] = await Promise.all([
    getKnowledgePoints(),
    getKnowledgePointRelations()
  ])
  return { points, relations }
}

/**
 * 创建知识点
 */
export const createKnowledgePoint = (data: any): Promise<KnowledgePoint> => {
  return request.post('/api/v1/knowledge/points', data)
}

/**
 * 创建知识点关系
 */
export const createKnowledgePointRelation = (data: any): Promise<KnowledgePointRelation> => {
  return request.post('/api/v1/knowledge/relations', data)
}

/**
 * 从知识点和关系生成图谱（前端处理）
 */
export const generateGraphFromData = (points: KnowledgePoint[], relations: KnowledgePointRelation[]): any => {
  // 使用ECharts所需的数据格式
  const nodes = points.map(point => ({
    id: point.id.toString(),
    name: point.title,
    value: point,
    category: 0,
    label: {
      show: true,
      formatter: point.title,
      position: 'right'
    },
    tooltip: {
      formatter: (params: any) => {
        const point = params.data.value as KnowledgePoint
        return `
          <div style="max-width: 300px;">
            <h4>${point.title}</h4>
            <p><strong>定义：</strong>${point.definition || '无'}</p>
            <p><strong>解释：</strong>${point.explanation || '无'}</p>
            ${point.formulaOrCode ? `<p><strong>公式/代码：</strong><code>${point.formulaOrCode}</code></p>` : ''}
            ${point.example ? `<p><strong>示例：</strong>${point.example}</p>` : ''}
          </div>
        `
      }
    }
  }))

  const edges = relations.map(relation => ({
    id: relation.id.toString(),
    source: relation.sourcePointId.toString(),
    target: relation.targetPointId.toString(),
    label: {
      show: true,
      formatter: relation.relationType
    },
    value: relation
  }))

  return {
    nodes,
    edges,
    categories: [{
      name: '知识点'
    }]
  }
}

/**
 * 检查后端API是否存在（用于测试）
 */
export const checkKnowledgeApi = () => {
  return request.get('/api/v1/knowledge/points', { validateStatus: (status) => status < 500 })
}
