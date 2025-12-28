import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  TouchableOpacity,
  Alert,
} from 'react-native';
import { colors } from '../constants/colors';
import { storage } from '../utils/storage';
import { authApi } from '../api/authApi';

export default function HomeScreen({ navigation }) {
  // ========== State ==========
  const [user, setUser] = useState(null);  // 사용자 정보

  // ========== useEffect ==========
  // 컴포넌트가 처음 렌더링될 때 실행
  // 웹 React의 componentDidMount와 비슷
  useEffect(() => {
    loadUser();
  }, []);  // [] 빈 배열: 처음 한 번만 실행

  // 저장된 사용자 정보 불러오기
  const loadUser = async () => {
    const userData = await storage.getUser();
    console.log('불러온 사용자 정보:', userData);
    setUser(userData);
  };

  // ========== 로그아웃 ==========
  const handleLogout = () => {
    // 확인 창 띄우기
    Alert.alert(
      '로그아웃',           // 제목
      '정말 로그아웃 하시겠습니까?',  // 메시지
      [
        // 버튼들
        {
          text: '취소',
          style: 'cancel',  // iOS에서 회색 버튼
        },
        {
          text: '로그아웃',
          style: 'destructive',  // iOS에서 빨간 버튼
          onPress: async () => {
            await authApi.logout();
            navigation.replace('Login');  // 로그인 화면으로 이동
          },
        },
      ]
    );
  };

  // ========== 악습 추가 버튼 ==========
  const handleAddHabit = () => {
    // 아직 구현 안 함 - 나중에 추가
    Alert.alert('준비 중', '악습 추가 기능은 곧 추가될 예정이에요!');
  };

  // ========== 금고 버튼 ==========
  const handleVaultPress = () => {
    // 아직 구현 안 함 - 나중에 추가
    Alert.alert('금고', '총 절약 금액: ₩0');
  };

  // ========== 화면 렌더링 ==========
  return (
    <SafeAreaView style={styles.container}>

      {/* ===== 상단 헤더 ===== */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>홈</Text>
        <TouchableOpacity onPress={handleLogout}>
          <Text style={styles.logoutText}>로그아웃</Text>
        </TouchableOpacity>
      </View>

      {/* ===== 메인 컨텐츠 ===== */}
      <View style={styles.content}>

        {/* 환영 메시지 */}
        <Text style={styles.welcomeText}>
          안녕하세요, {user?.nickname || '사용자'}님! 👋
        </Text>

        {/* 안내 메시지 */}
        <Text style={styles.descriptionText}>
          아직 등록된 악습이 없습니다.{'\n'}
          악습을 등록하고 절약을 시작해보세요!
        </Text>

        {/* 악습 추가 버튼 */}
        <TouchableOpacity
          style={styles.addButton}
          onPress={handleAddHabit}
        >
          <Text style={styles.addButtonText}>+ 악습 추가하기</Text>
        </TouchableOpacity>

      </View>

      {/* ===== 금고 버튼 (플로팅) ===== */}
      <TouchableOpacity
        style={styles.vaultButton}
        onPress={handleVaultPress}
      >
        <Text style={styles.vaultButtonText}>💰 금고: ₩0</Text>
      </TouchableOpacity>

    </SafeAreaView>
  );
}

// ========== 스타일 ==========
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },

  // 헤더
  header: {
    flexDirection: 'row',  // 가로 방향 배치
    justifyContent: 'space-between',  // 양 끝으로 배치
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: colors.white,
    // 그림자 (iOS)
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    // 그림자 (Android)
    elevation: 3,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: colors.black,
  },
  logoutText: {
    fontSize: 14,
    color: colors.gray,
  },

  // 메인 컨텐츠
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 24,
  },
  welcomeText: {
    fontSize: 22,
    fontWeight: '600',
    color: colors.black,
    marginBottom: 12,
  },
  descriptionText: {
    fontSize: 14,
    color: colors.gray,
    textAlign: 'center',
    lineHeight: 22,
    marginBottom: 32,
  },
  addButton: {
    backgroundColor: colors.primary,
    paddingHorizontal: 24,
    paddingVertical: 14,
    borderRadius: 12,
  },
  addButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.white,
  },

  // 금고 버튼 (플로팅)
  vaultButton: {
    position: 'absolute',  // 절대 위치 / 다른 요소와 관계없이 위치 지정
    bottom: 40,
    right: 20,
    backgroundColor: colors.primary,
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 24,
    // 그림자
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 6,
    elevation: 5,
  },
  vaultButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.white,
  },
});