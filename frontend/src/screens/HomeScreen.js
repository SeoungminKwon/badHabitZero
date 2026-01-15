import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  FlatList,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { getHabits, deleteHabit } from '../api/habitApi';
import { getCategoryByKey } from '../constants/categories';
import { storage } from '../utils/storage';

export default function HomeScreen({ navigation }) {
  const [user, setUser] = useState(null);
  const [habits, setHabits] = useState([]);
  const [loading, setLoading] = useState(true);

  // 화면에 포커스될 때마다 데이터 새로고침
  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [])
  );

  const loadData = async () => {
    try {
      setLoading(true);
      const userData = await storage.getUser();
      setUser(userData);

      const response = await getHabits();
      if (response.success) {
        setHabits(response.data);
      }
    } catch (error) {
      console.log('데이터 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteHabit = (habitId, habitName) => {
    Alert.alert(
      '악습 삭제',
      `"${habitName}"을(를) 삭제하시겠습니까?`,
      [
        { text: '취소', style: 'cancel' },
        {
          text: '삭제',
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteHabit(habitId);
              loadData(); // 새로고침
            } catch (error) {
              Alert.alert('오류', '삭제에 실패했습니다.');
            }
          },
        },
      ]
    );
  };

  const renderHabitItem = ({ item }) => {
    const category = getCategoryByKey(item.category);
    
    return (
      <TouchableOpacity
        style={styles.habitCard}
        onPress={() => navigation.navigate('HabitDetail', { habitId: item.id })}
        onLongPress={() => handleDeleteHabit(item.id, item.name)}
      >
        <View style={styles.habitLeft}>
          <Text style={styles.habitIcon}>{category.icon}</Text>
          <View>
            <Text style={styles.habitName}>{item.name}</Text>
            <Text style={styles.habitCategory}>{category.label}</Text>
          </View>
        </View>
        <View style={styles.habitRight}>
          <Text style={styles.habitValue}>
            {item.effectiveValue?.toLocaleString()}원
          </Text>
          <Text style={styles.habitValueLabel}>1회당</Text>
        </View>
      </TouchableOpacity>
    );
  };

  const renderEmptyList = () => (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyIcon}>🎯</Text>
      <Text style={styles.emptyTitle}>등록된 악습이 없어요</Text>
      <Text style={styles.emptySubtitle}>
        고치고 싶은 습관을 등록하고{'\n'}얼마나 절약할 수 있는지 확인해보세요!
      </Text>
    </View>
  );

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#4A90A4" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* 헤더 */}
      <View style={styles.header}>
        <Text style={styles.greeting}>
          안녕하세요, {user?.nickname || '사용자'}님! 👋
        </Text>
        <Text style={styles.subtitle}>
          오늘도 좋은 습관을 만들어볼까요?
        </Text>
      </View>

      {/* 악습 목록 */}
      <View style={styles.listContainer}>
        <View style={styles.listHeader}>
          <Text style={styles.listTitle}>내 악습 목록</Text>
          <Text style={styles.listCount}>{habits.length}개</Text>
        </View>

        <FlatList
          data={habits}
          renderItem={renderHabitItem}
          keyExtractor={(item) => item.id.toString()}
          ListEmptyComponent={renderEmptyList}
          contentContainerStyle={habits.length === 0 && styles.emptyList}
        />
      </View>

      {/* 악습 추가 버튼 */}
      <TouchableOpacity
        style={styles.addButton}
        onPress={() => navigation.navigate('AddHabit')}
      >
        <Text style={styles.addButtonText}>+ 악습 추가하기</Text>
      </TouchableOpacity>
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
    backgroundColor: '#4A90A4',
    paddingTop: 60,
    paddingBottom: 24,
    paddingHorizontal: 20,
  },
  greeting: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  subtitle: {
    fontSize: 14,
    color: '#E0E0E0',
    marginTop: 4,
  },
  listContainer: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 20,
  },
  listHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  listTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  listCount: {
    fontSize: 14,
    color: '#888',
  },
  habitCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  habitLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  habitIcon: {
    fontSize: 32,
    marginRight: 12,
  },
  habitName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  habitCategory: {
    fontSize: 12,
    color: '#888',
    marginTop: 2,
  },
  habitRight: {
    alignItems: 'flex-end',
  },
  habitValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#4A90A4',
  },
  habitValueLabel: {
    fontSize: 12,
    color: '#888',
  },
  emptyContainer: {
    alignItems: 'center',
    paddingVertical: 40,
  },
  emptyIcon: {
    fontSize: 48,
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 14,
    color: '#888',
    textAlign: 'center',
    lineHeight: 20,
  },
  emptyList: {
    flexGrow: 1,
  },
  addButton: {
    backgroundColor: '#4A90A4',
    marginHorizontal: 20,
    marginBottom: 30,
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  addButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
});