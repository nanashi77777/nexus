package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.lin037.nexus.infrastructure.adapter.knowledge.constant.KnowledgeAiPrompt;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgePoint;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgeRelation;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.KnowledgeRelationGenerationResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.ai.service.StructuredOutputTool;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 知识关系生成专项测试
 *
 * @author AI Assistant
 */
@Slf4j
@SpringBootTest
public class KnowledgeRelationGenerationTest {

    private static final String MODEL_NAME = "qwen-max";
    private static final String USED_FOR = "STRUCTURED_OUTPUT_EXPLANATION";
    /**
     * 创建示例对象用于指导AI返回正确的数据结构
     */
    private static final KnowledgeRelationGenerationResult RELATION_EXAMPLE = new KnowledgeRelationGenerationResult(
            List.of(
                    new AiKnowledgeRelation(1L, 2L, "PRE_REQUISITE", "理解源知识点是学习目标知识点的基础。"),
                    new AiKnowledgeRelation(2L, 3L, "POST_REQUISITE", "源知识点为目标知识点提供了必要的背景知识。")
            ),
            false
    );
    @Autowired
    private AiCoreService aiCoreService;
    @Autowired
    private StructuredOutputTool structuredOutputTool;

    @Test
    public void testKnowledgeRelationGeneration() {
        log.info("🧪 开始测试知识关系生成");

        // 创建测试用的知识点数据
        List<AiKnowledgePoint> knowledgePoints = createTestKnowledgePoints();

        log.info("📊 测试数据：");
        log.info("   知识点数量: {}", knowledgePoints.size());
        for (int i = 0; i < Math.min(5, knowledgePoints.size()); i++) {
            AiKnowledgePoint point = knowledgePoints.get(i);
            log.info("   知识点{}: {} (ID: {})", i + 1, point.getTitle(), point.getId());
        }
        if (knowledgePoints.size() > 5) {
            log.info("   ... 共{}个知识点", knowledgePoints.size());
        }

        // 获取ChatModel
        ChatModel chatModel = aiCoreService.getChatModel(MODEL_NAME, USED_FOR);
        TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();

        try {
            // 构建提示词
            KnowledgeAiPrompt.PromptPair promptPair = KnowledgeAiPrompt.RelationGenerator.build(
                    knowledgePoints,
                    new ArrayList<>() // 空的已生成关系列表
            );

            log.info("🤖 AI提示词构建完成");
            log.info("   系统提示词长度: {}", promptPair.systemPrompt().length());
            log.info("   用户提示词长度: {}", promptPair.userPrompt().length());

            // 调用AI生成关系
            log.info("🚀 开始调用AI生成关系...");
            StructResult<KnowledgeRelationGenerationResult> result = structuredOutputTool.generateStructuredOutput(
                    chatModel,
                    promptPair.systemPrompt(),
                    promptPair.userPrompt(),
                    RELATION_EXAMPLE,
                    KnowledgeRelationGenerationResult.class
            );

            // 统计Token使用量
            tokenAccumulator.add(result.getTokenUsage());

            log.info("📈 Token使用统计:");
            log.info("   输入Token: {}", result.getTokenUsage().inputTokenCount());
            log.info("   输出Token: {}", result.getTokenUsage().outputTokenCount());
            log.info("   总Token: {}", result.getTokenUsage().totalTokenCount());

            // 分析生成结果
            if (result.getResult() != null) {
                KnowledgeRelationGenerationResult relationResult = result.getResult();

                log.info("✅ AI响应解析成功");
                log.info("📊 生成结果分析:");
                log.info("   生成关系数量: {}", relationResult.getRelations() != null ? relationResult.getRelations().size() : 0);
                log.info("   是否完成: {}", relationResult.getIsComplete());

                // 详细分析每个关系
                if (relationResult.getRelations() != null && !relationResult.getRelations().isEmpty()) {
                    log.info("\n🔍 关系详情:");

                    int validCount = 0;
                    int invalidCount = 0;

                    for (int i = 0; i < relationResult.getRelations().size(); i++) {
                        AiKnowledgeRelation relation = relationResult.getRelations().get(i);
                        boolean isValid = isValidRelation(relation);

                        if (isValid) {
                            validCount++;
                            String sourceTitle = findKnowledgePointTitle(knowledgePoints, relation.getSourceKnowledgeId());
                            String targetTitle = findKnowledgePointTitle(knowledgePoints, relation.getTargetKnowledgeId());

                            log.info("   关系{}: ✅ 有效", i + 1);
                            log.info("     源: {} (ID: {})", sourceTitle, relation.getSourceKnowledgeId());
                            log.info("     目标: {} (ID: {})", targetTitle, relation.getTargetKnowledgeId());
                            log.info("     类型: {}", relation.getRelationType());
                            log.info("     描述: {}", relation.getRelationDescription());
                        } else {
                            invalidCount++;
                            log.info("   关系{}: ❌ 无效 - {}", i + 1, getInvalidReason(relation));
                        }
                        log.info("");
                    }

                    log.info("📈 质量统计:");
                    log.info("   有效关系: {} 条", validCount);
                    log.info("   无效关系: {} 条", invalidCount);
                    log.info("   有效率: {:.1f}%", validCount * 100.0 / relationResult.getRelations().size());

                    // 判断是否解决了空对象问题
                    if (invalidCount == 0) {
                        log.info("🎉 完美！没有发现空关系对象，问题已解决！");
                    } else if (validCount > 0) {
                        log.info("⚠️  部分关系有效，仍有改进空间");
                    } else {
                        log.info("❌ 所有关系都无效，问题仍然存在");
                    }

                } else {
                    log.info("❌ 没有生成任何关系");
                }

            } else {
                log.error("❌ AI响应解析失败，返回null");
            }

        } catch (Exception e) {
            log.error("❌ 测试过程中发生异常", e);
        }

        log.info("🏁 知识关系生成测试完成");
    }

    @Test
    public void testSmallBatchRelationGeneration() {
        log.info("🧪 测试小批量关系生成（减少token消耗）");

        // 只使用前10个知识点进行测试
        List<AiKnowledgePoint> knowledgePoints = createTestKnowledgePoints().subList(0, 10);

        log.info("📊 小批量测试数据：");
        log.info("   知识点数量: {}", knowledgePoints.size());

        ChatModel chatModel = aiCoreService.getChatModel(MODEL_NAME, USED_FOR);
        TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();

        try {
            // 构建提示词
            KnowledgeAiPrompt.PromptPair promptPair = KnowledgeAiPrompt.RelationGenerator.build(
                    knowledgePoints,
                    new ArrayList<>()
            );

            log.info("🤖 小批量提示词:");
            log.info("   系统提示词长度: {}", promptPair.systemPrompt().length());
            log.info("   用户提示词长度: {}", promptPair.userPrompt().length());

            // 调用AI生成关系
            StructResult<KnowledgeRelationGenerationResult> result = structuredOutputTool.generateStructuredOutput(
                    chatModel,
                    promptPair.systemPrompt(),
                    promptPair.userPrompt(),
                    RELATION_EXAMPLE,
                    KnowledgeRelationGenerationResult.class
            );

            tokenAccumulator.add(result.getTokenUsage());

            log.info("📈 小批量Token统计:");
            log.info("   输入Token: {}", result.getTokenUsage().inputTokenCount());
            log.info("   输出Token: {}", result.getTokenUsage().outputTokenCount());

            if (result.getResult() != null && result.getResult().getRelations() != null) {
                int validCount = 0;
                for (AiKnowledgeRelation relation : result.getResult().getRelations()) {
                    if (isValidRelation(relation)) {
                        validCount++;
                    }
                }

                log.info("📊 小批量结果:");
                log.info("   生成关系: {} 条", result.getResult().getRelations().size());
                log.info("   有效关系: {} 条", validCount);
                log.info("   成功率: {:.1f}%", validCount * 100.0 / result.getResult().getRelations().size());

                if (validCount > 0) {
                    log.info("✅ 小批量测试成功！建议采用分批处理策略");
                }
            }

        } catch (Exception e) {
            log.error("❌ 小批量测试异常", e);
        }
    }

    /**
     * 创建测试用的知识点数据
     */
    private List<AiKnowledgePoint> createTestKnowledgePoints() {
        return Arrays.asList(
                new AiKnowledgePoint(74671913149923328L, "大模型的基本概念", "大模型是指参数量巨大、能够处理复杂任务的深度学习模型。", "想象一下，大模型就像一个超级智能的大脑，它通过大量的数据和复杂的神经网络结构来学习和理解世界。这些模型通常包含数百万甚至数十亿个参数，可以完成从自然语言处理到图像识别等各种任务。", null, null),
                new AiKnowledgePoint(74671913158311938L, "大模型的发展历程", "大模型的发展经历了从简单的神经网络到复杂的深度学习模型的过程。", "大模型的发展就像是一场马拉松比赛。起初，我们只有简单的神经网络，它们只能解决一些基本的问题。随着时间的推移，计算能力的提升和数据量的增加，我们开始构建更复杂的模型，如卷积神经网络（CNN）和循环神经网络（RNN）。最终，我们迎来了像Transformer这样的革命性架构，使得大模型成为可能。", null, null),
                new AiKnowledgePoint(74671913158311939L, "大模型的重要性", "大模型在现代人工智能领域中具有重要的地位，能够推动技术进步和应用创新。", "大模型就像是现代科技的引擎，它们不仅能够提高现有应用的性能，还能开启新的应用场景。例如，在自然语言处理领域，大模型可以生成高质量的文章、进行多语言翻译，甚至与人类进行流畅的对话。在医疗、金融等领域，大模型也有广泛的应用前景。", null, null),
                new AiKnowledgePoint(74671913158311940L, "线性代数基础", "线性代数是研究向量空间和线性映射的一门数学分支。", "线性代数就像是构建高楼大厦的地基。它提供了处理高维数据的工具，如矩阵和向量。通过线性代数，我们可以进行数据变换、降维等操作，为后续的机器学习算法提供支持。", null, null),
                new AiKnowledgePoint(74671913158311941L, "概率论基础", "概率论是研究随机事件发生可能性的一门数学分支。", "概率论就像是预测未来的水晶球。它帮助我们理解和量化不确定性，从而做出更好的决策。在机器学习中，概率论用于建模数据分布、估计参数以及评估模型的不确定性。", null, null),
                new AiKnowledgePoint(74671913158311942L, "统计学基础", "统计学是研究如何收集、分析、解释和展示数据的一门学科。", "统计学就像是数据侦探，它帮助我们从大量数据中提取有用的信息。通过统计学方法，我们可以进行假设检验、参数估计和数据可视化，从而更好地理解数据背后的规律。", null, null),
                new AiKnowledgePoint(74671913158311943L, "线性回归模型", "线性回归是一种用于预测连续变量的统计模型。", "线性回归就像是用一条直线来拟合数据点。通过这条直线，我们可以预测未来数据的趋势。线性回归是最简单的机器学习模型之一，但它在许多实际问题中仍然非常有效。", null, null),
                new AiKnowledgePoint(74671913158311944L, "机器学习概述", "机器学习是人工智能的一个分支，通过让计算机从数据中学习来改进其性能。", "机器学习就像是教孩子骑自行车。一开始，孩子可能会摔倒很多次，但通过不断的练习和反馈，他们最终能够熟练地骑行。同样，机器学习模型通过不断的学习和调整，能够逐渐提高其在特定任务上的表现。", null, null),
                new AiKnowledgePoint(74671913158311945L, "机器学习的主要类型", "机器学习主要分为监督学习、无监督学习和强化学习三种类型。", "监督学习就像是有老师指导的学习过程，每个训练样本都有明确的标签；无监督学习则像是自学，没有明确的标签，需要自己发现数据中的模式；强化学习则是通过与环境互动来学习最优策略，类似于通过试错来学习。", null, null),
                new AiKnowledgePoint(74671913158311946L, "机器学习的基本流程", "机器学习的基本流程包括数据预处理、模型选择、训练、评估和优化。", "机器学习的基本流程就像是烹饪一道菜。首先，我们需要准备食材（数据预处理），然后选择合适的食谱（模型选择），接着按照食谱烹饪（训练），最后品尝并调整味道（评估和优化）。每一步都至关重要，缺一不可。", null, null),
                new AiKnowledgePoint(74671913158311947L, "常用机器学习算法简介", "常用的机器学习算法包括决策树、支持向量机、随机森林、神经网络等。", "常用的机器学习算法就像是不同的工具箱，每种工具都有其独特的用途。决策树适用于分类和回归任务，支持向量机擅长处理高维数据，随机森林通过集成多个决策树来提高准确性，而神经网络则可以处理复杂的非线性关系。", null, null),
                new AiKnowledgePoint(74671913158311948L, "评估与优化", "评估与优化是机器学习中确保模型性能的关键步骤。", "评估与优化就像是对模型进行体检和调养。通过评估指标（如准确率、召回率等），我们可以了解模型的表现，并通过调整超参数、改进特征工程等方法来优化模型，使其达到最佳状态。", null, null),
                new AiKnowledgePoint(74671913158311949L, "神经网络基础", "神经网络是一种模拟人脑神经元结构和功能的计算模型，用于处理复杂的非线性关系。", "想象一下，神经网络就像一个由许多小灯泡（神经元）组成的电路板。每个灯泡可以接收输入信号，并根据一定的规则决定是否点亮。这些灯泡通过连接线（权重）相互传递信息，最终形成一个能够学习和预测的系统。", null, null),
                new AiKnowledgePoint(74671913158311950L, "激活函数", "激活函数是神经网络中用来引入非线性特性的函数，使得神经网络能够学习和表示更复杂的模式。", "激活函数就像是给每个灯泡装上了一个开关，只有当输入信号达到一定强度时，灯泡才会点亮。常见的激活函数有Sigmoid、ReLU和Tanh等。", null, null),
                new AiKnowledgePoint(74671913158311951L, "卷积神经网络（CNN）", "卷积神经网络是一种专门用于处理具有网格结构数据（如图像）的神经网络架构。", "卷积神经网络就像是一个专门设计来处理图像的过滤器。它通过滑动一个小窗口（卷积核）在图像上移动，提取局部特征，并逐步组合成更高级别的特征。", null, null),
                new AiKnowledgePoint(74671913158311952L, "池化层", "池化层是卷积神经网络中的一种下采样技术，用于减少特征图的空间尺寸，同时保留主要特征。", "池化层就像是一个压缩工具，它可以将多个像素值合并为一个，从而减少数据量并提高计算效率。常见的池化方法有最大池化和平均池化。", null, null),
                new AiKnowledgePoint(74671913158311953L, "循环神经网络（RNN）", "循环神经网络是一种具有记忆功能的神经网络，适用于处理序列数据。", "循环神经网络就像是一个带有记忆功能的处理器，它可以记住之前的信息，并在处理新数据时考虑这些历史信息。这使得RNN非常适合处理文本、语音等序列数据。", null, null),
                new AiKnowledgePoint(74671913158311954L, "长短期记忆网络（LSTM）", "长短期记忆网络是一种特殊的循环神经网络，通过引入门控机制来解决长期依赖问题。", "LSTM就像是一个带有阀门的记忆单元，可以通过控制输入门、输出门和遗忘门来选择性地存储和读取信息。这种机制使得LSTM能够更好地处理长序列数据。", null, null),
                new AiKnowledgePoint(74671913158311955L, "深度学习的应用实例", "深度学习已经在多个领域取得了显著的应用成果，包括计算机视觉、自然语言处理、语音识别等。", "深度学习的应用非常广泛，从自动驾驶汽车到智能助手，从医疗影像分析到金融风险评估，几乎涵盖了所有需要处理复杂数据的任务。这些应用不仅提高了效率，还带来了新的创新机会。", null, null),
                new AiKnowledgePoint(74671913158311956L, "大模型训练概述", "大模型训练是指通过大量数据和计算资源来训练具有巨大参数量的深度学习模型的过程。", "大模型训练就像建造一座高楼大厦，需要大量的建筑材料（数据）和工人（计算资源）。这个过程非常复杂，但最终可以构建出能够处理各种复杂任务的强大模型。", null, null),
                new AiKnowledgePoint(74671913158311957L, "数据预处理的重要性", "数据预处理是将原始数据转换为适合机器学习算法输入格式的过程。", "数据预处理就像是厨师在烹饪前对食材进行清洗、切割和调味。只有经过精心准备的数据才能被模型有效地利用。", null, null),
                new AiKnowledgePoint(74671913158311958L, "数据清洗", "数据清洗是指去除或修正数据中的错误、不一致和缺失值的过程。", "数据清洗就像是洗衣服，去除污渍和杂质，使数据更加干净和可靠。", null, null),
                new AiKnowledgePoint(74671913158311959L, "特征工程", "特征工程是从原始数据中提取有用特征以提高模型性能的过程。", "特征工程就像是从矿石中提炼出有价值的金属。通过选择和构造合适的特征，可以使模型更好地理解和预测数据。", null, null),
                new AiKnowledgePoint(74671913158311960L, "超参数调优", "超参数调优是指调整模型的超参数以优化模型性能的过程。", "超参数调优就像是调音师调整乐器的音准，通过不断尝试不同的设置，找到最佳的配置，使模型表现得更好。", null, null),
                new AiKnowledgePoint(74671913158311961L, "网格搜索", "网格搜索是一种系统地遍历所有可能的超参数组合以找到最优配置的方法。", "网格搜索就像是在一个网格上逐个检查每个格子，确保没有遗漏任何一个可能的最佳配置。", null, null),
                new AiKnowledgePoint(74671913158311962L, "随机搜索", "随机搜索是一种通过随机采样超参数组合来寻找最优配置的方法。", "随机搜索就像是在一片广阔的森林中随机选取一些树木进行检查，虽然不是系统性的，但可以在较短时间内找到较好的配置。", null, null),
                new AiKnowledgePoint(74671913158311963L, "贝叶斯优化", "贝叶斯优化是一种基于概率模型的超参数调优方法，通过构建一个代理模型来指导搜索过程。", "贝叶斯优化就像是有一个智能助手，它会根据之前的搜索结果来预测哪些区域更有可能包含最优配置，并优先搜索这些区域。", null, null),
                new AiKnowledgePoint(74671913158311964L, "分布式训练", "分布式训练是指利用多台计算机协同工作来加速模型训练的过程。", "分布式训练就像是多个工人一起协作完成一项大型工程，每个人负责一部分工作，从而大大提高整体效率。", null, null),
                new AiKnowledgePoint(74671913158311965L, "数据并行", "数据并行是指将数据分成多个部分，每部分由不同的计算节点处理，然后合并结果。", "数据并行就像是将一本书分成多份，让多个读者同时阅读不同部分，最后汇总他们的理解。", null, null),
                new AiKnowledgePoint(74671913158311966L, "模型并行", "模型并行是指将模型的不同部分分配到不同的计算节点上进行处理。", "模型并行就像是将一个复杂的机械装置拆分成多个模块，每个模块由不同的工人组装，最后再拼接在一起。", null, null),
                new AiKnowledgePoint(74671913158311967L, "混合并行", "混合并行是指结合数据并行和模型并行的方法，以充分利用计算资源。", "混合并行就像是既分工又合作，一部分人负责读取书的不同部分，另一部分人负责组装机械的不同模块，最终共同完成任务。", null, null),
                new AiKnowledgePoint(74671913158311968L, "优化技巧与实践", "优化技巧与实践是指在训练过程中应用的各种技术和方法，以提高模型的性能和效率。", "优化技巧与实践就像是赛车手在比赛中使用各种驾驶技巧和策略，以最快的速度到达终点。", null, null),
                new AiKnowledgePoint(74671913158311969L, "梯度裁剪", "梯度裁剪是一种防止梯度爆炸的技术，通过限制梯度的大小来稳定训练过程。", "梯度裁剪就像是给汽车安装限速器，防止车速过快导致失控。", null, null),
                new AiKnowledgePoint(74671913158311970L, "学习率衰减", "学习率衰减是指在训练过程中逐渐降低学习率，以帮助模型更好地收敛。", "学习率衰减就像是在跑步时逐渐减速，以便更平稳地达到终点。", null, null),
                new AiKnowledgePoint(74671913158311971L, "权重初始化", "权重初始化是指在训练开始前为模型的权重设置初始值，以影响模型的收敛速度和性能。", "权重初始化就像是在比赛前为运动员做好热身，确保他们在比赛中能够发挥最佳状态。", null, null),
                new AiKnowledgePoint(74671913158311972L, "在线课程与教程", "在线课程与教程是指通过互联网提供的系统化学习资源，帮助学习者掌握特定领域的知识和技能。", "在线课程与教程通常包括视频讲座、阅读材料、实践项目和测验。这些资源可以帮助初学者从零开始逐步掌握大模型的相关知识，并提供进阶内容以深入理解前沿技术。", null, null),
                new AiKnowledgePoint(74671913158311973L, "开源库与框架", "开源库与框架是公开可用的软件工具包，为开发者提供了实现复杂算法和模型的基础架构。", "开源库与框架简化了开发过程，使得研究人员和工程师能够快速构建和测试模型。常见的开源库如TensorFlow和PyTorch提供了丰富的功能和社区支持，加速了大模型的研究和应用。", null, null),
                new AiKnowledgePoint(74671913158311974L, "数据集资源", "数据集资源是指用于训练和测试机器学习模型的数据集合，涵盖了各种类型和规模的数据。", "高质量的数据集对于训练有效的机器学习模型至关重要。常用的数据集资源包括图像、文本、语音等多种类型的数据，这些数据集通常由学术机构或公司发布，并且可以在公共平台上免费获取。", null, null),
                new AiKnowledgePoint(74671913158311975L, "社区与论坛", "社区与论坛是供学习者和技术人员交流经验、分享资源和解决问题的在线平台。", "社区与论坛为学习者提供了宝贵的互动机会，可以通过提问、讨论和参与项目来加深对大模型的理解。这些平台还经常举办线上活动和竞赛，促进知识的传播和创新。", null, null)
        );
    }

    /**
     * 验证关系对象是否包含完整的必要字段
     */
    private boolean isValidRelation(AiKnowledgeRelation relation) {
        return relation != null &&
                relation.getSourceKnowledgeId() != null &&
                relation.getTargetKnowledgeId() != null &&
                relation.getRelationType() != null &&
                !relation.getRelationType().trim().isEmpty() &&
                relation.getRelationDescription() != null &&
                !relation.getRelationDescription().trim().isEmpty();
    }

    /**
     * 获取无效关系的原因
     */
    private String getInvalidReason(AiKnowledgeRelation relation) {
        if (relation == null) {
            return "关系对象为null";
        }
        if (relation.getSourceKnowledgeId() == null) {
            return "缺少源知识点ID";
        }
        if (relation.getTargetKnowledgeId() == null) {
            return "缺少目标知识点ID";
        }
        if (relation.getRelationType() == null || relation.getRelationType().trim().isEmpty()) {
            return "缺少或为空的关系类型";
        }
        if (relation.getRelationDescription() == null || relation.getRelationDescription().trim().isEmpty()) {
            return "缺少或为空的关系描述";
        }
        return "未知原因";
    }

    /**
     * 根据ID查找知识点标题
     */
    private String findKnowledgePointTitle(List<AiKnowledgePoint> knowledgePoints, Long id) {
        return knowledgePoints.stream()
                .filter(point -> point.getId().equals(id))
                .map(AiKnowledgePoint::getTitle)
                .findFirst()
                .orElse("未知知识点");
    }
}
