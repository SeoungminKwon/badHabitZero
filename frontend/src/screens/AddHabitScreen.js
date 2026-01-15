import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { CATEGORIES } from '../constants/categories';
import { analyzeHabit, calculateValue, createHabit } from '../api/habitApi';

export default function AddHabitScreen({ navigation }) {
  const [step, setStep] = useState(1); // 1: 기본정보, 2: AI질문, 3: 결과
  const [loading, setLoading] = useState(false);

  // 기본 정보
  const [name, setName] = useState('');
  const [category, setCategory] = useState(null);
  const [reason, setReason] = useState('');

  // AI 질문/답변
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});

  // AI 결과
  const [valueResult, setValueResult] = useState(null);

  // 1단계: AI 분석 요청
  const handleAnalyze = async () => {
    if (!name.trim()) {
      Alert.alert('알림', '악습 이름을 입력해주세요.');
      return;
    }
    if (!category) {
      Alert.alert('알림', '카테고리를 선택해주세요.');
      return;
    }

    setLoading(true);
    try {
      const response = await analyzeHabit({
        habitName: name,
        category: category,
        reason: reason,
      });

      if (response.success && response.data.questions) {
        setQuestions(response.data.questions);
        setStep(2);
      }
    } catch (error) {
      console.error('AI 분석 실패:', error);
      Alert.alert('오류', 'AI 분석에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  // 2단계: 가치 산정 요청
  const handleCalculateValue = async () => {
    // 모든 질문에 답변했는지 확인
    const unanswered = questions.filter(q => !answers[q.id]);
    if (unanswered.length > 0) {
      Alert.alert('알림', '모든 질문에 답변해주세요.');
      return;
    }

    setLoading(true);
    try {
      const response = await calculateValue({
        habitName: name,
        category: category,
        reason: reason,
        answers: answers,
      });

      if (response.success) {
        setValueResult(response.data);
        setStep(3);
      }
    } catch (error) {
      console.error('가치 산정 실패:', error);
      Alert.alert('오류', '가치 산정에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  // 3단계: 악습 등록
  const handleCreateHabit = async () => {
    setLoading(true);
    try {
      const categoryInfo = CATEGORIES.find(c => c.key === category);
      
      const response = await createHabit({
        name: name,
        category: category,
        reason: reason,
        icon: categoryInfo?.icon || '📌',
        baseValue: valueResult?.value || 10000,
        aiValue: valueResult?.value,
        aiDescription: valueResult?.explanation,
      });

      if (response.success) {
        Alert.alert('완료', '악습이 등록되었습니다!', [
          { text: '확인', onPress: () => navigation.goBack() }
        ]);
      }
    } catch (error) {
      console.error('악습 등록 실패:', error);
      Alert.alert('오류', '등록에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  // 답변 업데이트
  const updateAnswer = (questionId, value) => {
    setAnswers(prev => ({ ...prev, [questionId]: value }));
  };

  // 1단계: 기본 정보 입력
  const renderStep1 = () => (
    <ScrollView style={styles.stepContainer}>
      <Text style={styles.stepTitle}>어떤 악습을 고치고 싶으세요?</Text>

      <Text style={styles.label}>악습 이름</Text>
      <TextInput
        style={styles.input}
        placeholder="예: 야식, 충동구매, 유튜브 시청"
        value={name}
        onChangeText={setName}
      />

      <Text style={styles.label}>카테고리</Text>
      <View style={styles.categoryGrid}>
        {CATEGORIES.map((cat) => (
          <TouchableOpacity
            key={cat.key}
            style={[
              styles.categoryButton,
              category === cat.key && styles.categoryButtonActive,
            ]}
            onPress={() => setCategory(cat.key)}
          >
            <Text style={styles.categoryIcon}>{cat.icon}</Text>
            <Text
              style={[
                styles.categoryLabel,
                category === cat.key && styles.categoryLabelActive,
              ]}
            >
              {cat.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      <Text style={styles.label}>고치고 싶은 이유 (선택)</Text>
      <TextInput
        style={[styles.input, styles.textArea]}
        placeholder="예: 건강이 걱정돼요, 돈이 너무 많이 나가요"
        value={reason}
        onChangeText={setReason}
        multiline
        numberOfLines={3}
      />

      <TouchableOpacity
        style={styles.primaryButton}
        onPress={handleAnalyze}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator color="#FFFFFF" />
        ) : (
          <Text style={styles.primaryButtonText}>다음 단계로</Text>
        )}
      </TouchableOpacity>
    </ScrollView>
  );

  // 2단계: AI 질문 답변
  const renderStep2 = () => (
    <ScrollView style={styles.stepContainer}>
      <Text style={styles.stepTitle}>몇 가지 질문이 있어요 🤔</Text>
      <Text style={styles.stepSubtitle}>
        정확한 가치 산정을 위해 답변해주세요
      </Text>

      {questions.map((question) => (
        <View key={question.id} style={styles.questionContainer}>
          <Text style={styles.questionText}>{question.question}</Text>
          
          {question.type === 'choice' && question.options ? (
            <View style={styles.optionsContainer}>
              {question.options.map((option) => (
                <TouchableOpacity
                  key={option}
                  style={[
                    styles.optionButton,
                    answers[question.id] === option && styles.optionButtonActive,
                  ]}
                  onPress={() => updateAnswer(question.id, option)}
                >
                  <Text
                    style={[
                      styles.optionText,
                      answers[question.id] === option && styles.optionTextActive,
                    ]}
                  >
                    {option}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          ) : (
            <TextInput
              style={styles.input}
              placeholder="숫자를 입력해주세요"
              value={answers[question.id]?.toString() || ''}
              onChangeText={(text) => updateAnswer(question.id, Number(text) || text)}
              keyboardType={question.type === 'number' ? 'numeric' : 'default'}
            />
          )}
        </View>
      ))}

      <TouchableOpacity
        style={styles.primaryButton}
        onPress={handleCalculateValue}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator color="#FFFFFF" />
        ) : (
          <Text style={styles.primaryButtonText}>가치 산정하기</Text>
        )}
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.secondaryButton}
        onPress={() => setStep(1)}
      >
        <Text style={styles.secondaryButtonText}>이전으로</Text>
      </TouchableOpacity>
    </ScrollView>
  );

  // 3단계: 결과 및 등록
  const renderStep3 = () => (
    <ScrollView style={styles.stepContainer}>
      <Text style={styles.stepTitle}>가치 산정 완료! 💰</Text>

      <View style={styles.resultCard}>
        <Text style={styles.resultLabel}>1회당 예상 손실</Text>
        <Text style={styles.resultValue}>
          {valueResult?.value?.toLocaleString()}원
        </Text>

        <View style={styles.breakdownContainer}>
          <View style={styles.breakdownItem}>
            <Text style={styles.breakdownLabel}>직접 비용</Text>
            <Text style={styles.breakdownValue}>
              {valueResult?.breakdown?.directCost?.toLocaleString()}원
            </Text>
          </View>
          <View style={styles.breakdownItem}>
            <Text style={styles.breakdownLabel}>건강 비용</Text>
            <Text style={styles.breakdownValue}>
              {valueResult?.breakdown?.healthCost?.toLocaleString()}원
            </Text>
          </View>
          <View style={styles.breakdownItem}>
            <Text style={styles.breakdownLabel}>기회 비용</Text>
            <Text style={styles.breakdownValue}>
              {valueResult?.breakdown?.opportunityCost?.toLocaleString()}원
            </Text>
          </View>
          <View style={styles.breakdownItem}>
            <Text style={styles.breakdownLabel}>심리 비용</Text>
            <Text style={styles.breakdownValue}>
              {valueResult?.breakdown?.psychologicalCost?.toLocaleString()}원
            </Text>
          </View>
        </View>

        <View style={styles.explanationContainer}>
          <Text style={styles.explanationTitle}>💡 AI 분석</Text>
          <Text style={styles.explanationText}>{valueResult?.explanation}</Text>
        </View>

        {valueResult?.sources && (
          <View style={styles.sourcesContainer}>
            <Text style={styles.sourcesTitle}>📚 참고 자료</Text>
            {valueResult.sources.map((source, index) => (
              <Text key={index} style={styles.sourceText}>• {source}</Text>
            ))}
          </View>
        )}
      </View>

      <TouchableOpacity
        style={styles.primaryButton}
        onPress={handleCreateHabit}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator color="#FFFFFF" />
        ) : (
          <Text style={styles.primaryButtonText}>이 가치로 등록하기</Text>
        )}
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.secondaryButton}
        onPress={() => setStep(2)}
      >
        <Text style={styles.secondaryButtonText}>다시 산정하기</Text>
      </TouchableOpacity>
    </ScrollView>
  );

  return (
    <View style={styles.container}>
      {/* 진행 표시 */}
      <View style={styles.progressContainer}>
        {[1, 2, 3].map((s) => (
          <View
            key={s}
            style={[
              styles.progressDot,
              step >= s && styles.progressDotActive,
            ]}
          />
        ))}
      </View>

      {step === 1 && renderStep1()}
      {step === 2 && renderStep2()}
      {step === 3 && renderStep3()}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  progressContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    paddingTop: 60,
    paddingBottom: 20,
    backgroundColor: '#FFFFFF',
  },
  progressDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#E0E0E0',
    marginHorizontal: 6,
  },
  progressDotActive: {
    backgroundColor: '#4A90A4',
  },
  stepContainer: {
    flex: 1,
    padding: 20,
  },
  stepTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  stepSubtitle: {
    fontSize: 14,
    color: '#888',
    marginBottom: 24,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 8,
    marginTop: 16,
  },
  input: {
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
    padding: 14,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#E0E0E0',
  },
  textArea: {
    height: 80,
    textAlignVertical: 'top',
  },
  categoryGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginHorizontal: -4,
  },
  categoryButton: {
    width: '31%',
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
    padding: 12,
    margin: '1%',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#E0E0E0',
  },
  categoryButtonActive: {
    borderColor: '#4A90A4',
    backgroundColor: '#F0F8FF',
  },
  categoryIcon: {
    fontSize: 24,
    marginBottom: 4,
  },
  categoryLabel: {
    fontSize: 12,
    color: '#666',
  },
  categoryLabelActive: {
    color: '#4A90A4',
    fontWeight: '600',
  },
  questionContainer: {
    marginBottom: 20,
  },
  questionText: {
    fontSize: 16,
    color: '#333',
    marginBottom: 12,
    lineHeight: 22,
  },
  optionsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  optionButton: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    paddingVertical: 10,
    paddingHorizontal: 16,
    marginRight: 8,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: '#E0E0E0',
  },
  optionButtonActive: {
    backgroundColor: '#4A90A4',
    borderColor: '#4A90A4',
  },
  optionText: {
    fontSize: 14,
    color: '#666',
  },
  optionTextActive: {
    color: '#FFFFFF',
  },
  resultCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 24,
    marginBottom: 20,
  },
  resultLabel: {
    fontSize: 14,
    color: '#888',
    textAlign: 'center',
  },
  resultValue: {
    fontSize: 40,
    fontWeight: 'bold',
    color: '#4A90A4',
    textAlign: 'center',
    marginVertical: 8,
  },
  breakdownContainer: {
    marginTop: 20,
    paddingTop: 20,
    borderTopWidth: 1,
    borderTopColor: '#E0E0E0',
  },
  breakdownItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  breakdownLabel: {
    fontSize: 14,
    color: '#666',
  },
  breakdownValue: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
  },
  explanationContainer: {
    marginTop: 20,
    padding: 16,
    backgroundColor: '#F5F5F5',
    borderRadius: 8,
  },
  explanationTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 8,
  },
  explanationText: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
  sourcesContainer: {
    marginTop: 16,
  },
  sourcesTitle: {
    fontSize: 12,
    fontWeight: '600',
    color: '#888',
    marginBottom: 4,
  },
  sourceText: {
    fontSize: 12,
    color: '#888',
  },
  primaryButton: {
    backgroundColor: '#4A90A4',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 24,
  },
  primaryButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  secondaryButton: {
    paddingVertical: 16,
    alignItems: 'center',
    marginTop: 12,
  },
  secondaryButtonText: {
    color: '#888',
    fontSize: 14,
  },
});