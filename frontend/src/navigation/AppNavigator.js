import React, { useEffect, useState } from 'react';
import { View, ActivityIndicator, StyleSheet } from 'react-native';
// 네비게이션 라이브러리
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
// 화면들
import LoginScreen from '../screens/LoginScreen';
import HomeScreen from '../screens/HomeScreen';
import OnboardingScreen from '../screens/OnboardingScreen';
// 유틸리티
import { storage } from '../utils/storage';
import { colors } from '../constants/colors';

// Stack Navigator 생성
// Stack: 화면을 쌓아가는 방식 (카드 쌓기처럼)
const Stack = createNativeStackNavigator();

export default function AppNavigator() {
  // ========== State ==========
  const [isLoading, setIsLoading] = useState(true);    // 로딩 중인지
  const [isLoggedIn, setIsLoggedIn] = useState(false); // 로그인 됐는지

  // ========== 앱 시작 시 로그인 상태 확인 ==========
  useEffect(() => {
    checkLoginStatus();
  }, []);

  const checkLoginStatus = async () => {
    try {
      // 저장된 토큰이 있는지 확인
      const token = await storage.getAccessToken();
      console.log('저장된 토큰:', token ? '있음' : '없음');

      // 토큰이 있으면 로그인 상태
      setIsLoggedIn(!!token);  // !!: truthy/falsy를 true/false로 변환
    } catch (error) {
      console.error('로그인 상태 확인 실패:', error);
      setIsLoggedIn(false);
    } finally {
      setIsLoading(false);
    }
  };

  // ========== 로딩 화면 ==========
  // 토큰 확인 중일 때 표시
  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  // ========== 메인 네비게이션 ==========
  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          headerShown: false,  // 기본 헤더 숨기기 (우리가 직접 만들 거라서)
        }}
      >
        {isLoggedIn ? (
          // ===== 로그인 된 상태 =====
          // Home이 첫 화면
          <>
            <Stack.Screen name="Home" component={HomeScreen} />
            <Stack.Screen name="Onboarding" component={OnboardingScreen} />
            <Stack.Screen name="Login" component={LoginScreen} />
          </>
        ) : (
          // ===== 로그인 안 된 상태 =====
          // Login이 첫 화면
          <>
            <Stack.Screen name="Login" component={LoginScreen} />
            <Stack.Screen name="Onboarding" component={OnboardingScreen} />
            <Stack.Screen name="Home" component={HomeScreen} />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}

// ========== 스타일 ==========
const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.white,
  },
});
