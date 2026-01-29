import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { getHabit, deleteHabit } from '../api/habitApi';
import { getCategoryByKey } from '../constants/categories';

export default function HabitDetailScreen({ route, navigation }) {
  const { habitId } = route.params;
  const [habit, setHabit] = useState(null);
  const [loading, setLoading] = useState(true);

  useFocusEffect(
    useCallback(() => {
      loadHabit();
    }, [habitId])
  );

  const loadHabit = async () => {
    try {
      setLoading(true);
      const response = await getHabit(habitId);
      if (response.success) {
        setHabit(response.data);
      }
    } catch (error) {
      console.error('악습 조회 실패:', error);
      Alert.alert('오류', '악습을 불러오지 못했습니다.', [
        { text: '확인', onPress: () => navigation.goBack() }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = () => {
    Alert.alert(
      '악습 삭제',
      `"${habit.name}"을(를) 삭제하시겠습니까?`,
      [
        { text: '취소', style: 'cancel' },
        {
          text: '삭제',
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteHabit(habitId);
              navigation.goBack();
            } catch (error) {
              Alert.alert('오류', '삭제에 실패했습니다.');
            }
          },
        },
      ]
    );
  };

  const handleEdit = () => {
    navigation.navigate('EditHabit', { habitId, habit });
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#4A90A4" />
      </View>
    );
  }

  if (!habit) {
    return null;
  }

  const category = getCategoryByKey(habit.category);

  return (
    <View style={styles.container}>
      {/* 헤더 */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
          <Text style={styles.backButtonText}>← 뒤로</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>악습 상세</Text>
        <View style={styles.headerRight} />
      </View>

      <ScrollView style={styles.content}>
        {/* 악습 정보 카드 */}
        <View style={styles.infoCard}>
          <View style={styles.infoHeader}>
            <Text style={styles.habitIcon}>{category.icon}</Text>
            <View style={styles.habitInfo}>
              <Text style={styles.habitName}>{habit.name}</Text>
              <Text style={styles.habitCategory}>{category.label}</Text>
            </View>
          </View>
          {habit.reason && (
            <View style={styles.reasonContainer}>
              <Text style={styles.reasonLabel}>고치고 싶은 이유</Text>
              <Text style={styles.reasonText}>{habit.reason}</Text>
            </View>
          )}
        </View>

        {/* 가치 산정 결과 */}
        <View style={styles.valueCard}>
          <Text style={styles.valueLabel}>1회당 예상 손실</Text>
          <Text style={styles.valueAmount}>
            {habit.effectiveValue?.toLocaleString()}원
          </Text>

          {habit.aiDescription && (
            <View style={styles.aiContainer}>
              <Text style={styles.aiTitle}>AI 분석</Text>
              <Text style={styles.aiText}>{habit.aiDescription}</Text>
            </View>
          )}
        </View>

        {/* 등록일 */}
        <View style={styles.dateCard}>
          <Text style={styles.dateLabel}>등록일</Text>
          <Text style={styles.dateText}>
            {habit.createdAt ? new Date(habit.createdAt).toLocaleDateString('ko-KR') : '-'}
          </Text>
        </View>
      </ScrollView>

      {/* 하단 버튼 */}
      <View style={styles.buttonContainer}>
        <TouchableOpacity style={styles.editButton} onPress={handleEdit}>
          <Text style={styles.editButtonText}>수정하기</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.deleteButton} onPress={handleDelete}>
          <Text style={styles.deleteButtonText}>삭제하기</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5F5F5',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 60,
    paddingBottom: 16,
    paddingHorizontal: 20,
    backgroundColor: '#4A90A4',
  },
  backButton: {
    padding: 4,
  },
  backButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  headerRight: {
    width: 60,
  },
  content: {
    flex: 1,
    padding: 20,
  },
  infoCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    marginBottom: 16,
  },
  infoHeader: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  habitIcon: {
    fontSize: 48,
    marginRight: 16,
  },
  habitInfo: {
    flex: 1,
  },
  habitName: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#333',
  },
  habitCategory: {
    fontSize: 14,
    color: '#888',
    marginTop: 4,
  },
  reasonContainer: {
    marginTop: 16,
    paddingTop: 16,
    borderTopWidth: 1,
    borderTopColor: '#E0E0E0',
  },
  reasonLabel: {
    fontSize: 12,
    color: '#888',
    marginBottom: 4,
  },
  reasonText: {
    fontSize: 14,
    color: '#333',
    lineHeight: 20,
  },
  valueCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    marginBottom: 16,
    alignItems: 'center',
  },
  valueLabel: {
    fontSize: 14,
    color: '#888',
  },
  valueAmount: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#4A90A4',
    marginVertical: 8,
  },
  aiContainer: {
    marginTop: 16,
    paddingTop: 16,
    borderTopWidth: 1,
    borderTopColor: '#E0E0E0',
    width: '100%',
  },
  aiTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 8,
  },
  aiText: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
  dateCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  dateLabel: {
    fontSize: 14,
    color: '#888',
  },
  dateText: {
    fontSize: 14,
    color: '#333',
  },
  buttonContainer: {
    flexDirection: 'row',
    padding: 20,
    gap: 12,
    backgroundColor: '#FFFFFF',
    borderTopWidth: 1,
    borderTopColor: '#E0E0E0',
  },
  editButton: {
    flex: 1,
    backgroundColor: '#4A90A4',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  editButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  deleteButton: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#F44336',
  },
  deleteButtonText: {
    color: '#F44336',
    fontSize: 16,
    fontWeight: 'bold',
  },
});
