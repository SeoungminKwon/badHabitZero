import React, { useState, useEffect } from 'react';
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
import { getHabit, updateHabit } from '../api/habitApi';

export default function EditHabitScreen({ route, navigation }) {
  const { habitId, habit: initialHabit } = route.params;
  const [loading, setLoading] = useState(!initialHabit);
  const [saving, setSaving] = useState(false);

  const [name, setName] = useState(initialHabit?.name || '');
  const [category, setCategory] = useState(initialHabit?.category || null);
  const [reason, setReason] = useState(initialHabit?.reason || '');

  useEffect(() => {
    if (!initialHabit) {
      loadHabit();
    }
  }, [habitId]);

  const loadHabit = async () => {
    try {
      setLoading(true);
      const response = await getHabit(habitId);
      if (response.success) {
        const data = response.data;
        setName(data.name);
        setCategory(data.category);
        setReason(data.reason || '');
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

  const handleSave = async () => {
    if (!name.trim()) {
      Alert.alert('알림', '악습 이름을 입력해주세요.');
      return;
    }
    if (!category) {
      Alert.alert('알림', '카테고리를 선택해주세요.');
      return;
    }

    setSaving(true);
    try {
      const categoryInfo = CATEGORIES.find(c => c.key === category);

      const response = await updateHabit(habitId, {
        name: name.trim(),
        category: category,
        reason: reason.trim(),
        icon: categoryInfo?.icon || '📌',
      });

      if (response.success) {
        Alert.alert('완료', '악습이 수정되었습니다.', [
          { text: '확인', onPress: () => navigation.goBack() }
        ]);
      }
    } catch (error) {
      console.error('악습 수정 실패:', error);
      Alert.alert('오류', '수정에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setSaving(false);
    }
  };

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
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
          <Text style={styles.backButtonText}>취소</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>악습 수정</Text>
        <TouchableOpacity onPress={handleSave} style={styles.saveButton} disabled={saving}>
          {saving ? (
            <ActivityIndicator size="small" color="#FFFFFF" />
          ) : (
            <Text style={styles.saveButtonText}>저장</Text>
          )}
        </TouchableOpacity>
      </View>

      <ScrollView style={styles.content}>
        {/* 악습 이름 */}
        <Text style={styles.label}>악습 이름</Text>
        <TextInput
          style={styles.input}
          placeholder="예: 야식, 충동구매, 유튜브 시청"
          value={name}
          onChangeText={setName}
        />

        {/* 카테고리 */}
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

        {/* 이유 */}
        <Text style={styles.label}>고치고 싶은 이유 (선택)</Text>
        <TextInput
          style={[styles.input, styles.textArea]}
          placeholder="예: 건강이 걱정돼요, 돈이 너무 많이 나가요"
          value={reason}
          onChangeText={setReason}
          multiline
          numberOfLines={3}
        />

        {/* 안내 문구 */}
        <View style={styles.infoBox}>
          <Text style={styles.infoText}>
            💡 AI 가치 산정 결과는 수정되지 않습니다.{'\n'}
            가치를 다시 산정하려면 악습을 삭제 후 새로 등록해주세요.
          </Text>
        </View>
      </ScrollView>
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
  saveButton: {
    padding: 4,
    minWidth: 40,
    alignItems: 'center',
  },
  saveButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
  },
  content: {
    flex: 1,
    padding: 20,
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
  infoBox: {
    marginTop: 24,
    padding: 16,
    backgroundColor: '#FFF9E6',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#FFE082',
  },
  infoText: {
    fontSize: 13,
    color: '#666',
    lineHeight: 20,
  },
});
