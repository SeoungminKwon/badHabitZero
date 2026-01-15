import React, { useState } from 'react';
// React Native 기본 컴포넌트들
import {
  View,           // div 같은 것
  Text,           // p, span 같은 것
  TouchableOpacity, // 클릭 가능한 버튼
  StyleSheet,     // CSS 스타일 정의
  SafeAreaView,   // 아이폰 노치 영역 피하기
  ActivityIndicator, // 로딩 스피너
  Modal,          // 팝업 창
  Alert,          // 알림 창
} from 'react-native';
// 웹페이지를 앱 안에서 보여주는 컴포넌트
import { WebView } from 'react-native-webview';
// 우리가 만든 파일들
import { colors } from '../constants/colors';
import { authApi } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function LoginScreen({ navigation }) {
  const { login } = useAuth();
  // ========== State (상태) ==========
  // useState: 컴포넌트 내에서 변하는 값을 관리
  const [loading, setLoading] = useState(false);        // 로딩 중인지
  const [showWebView, setShowWebView] = useState(false); // 웹뷰 보여줄지
  const [loginUrl, setLoginUrl] = useState('');         // 카카오 로그인 URL

  // ========== 카카오 로그인 버튼 클릭 ==========
  const handleKakaoLogin = async () => {
    try {
      setLoading(true);

      // 1. 서버에서 카카오 로그인 URL 받아오기
      const url = await authApi.getKakaoLoginUrl();
      console.log('카카오 로그인 URL:', url);

      // 2. URL 저장하고 WebView 열기
      setLoginUrl(url);
      setShowWebView(true);

    } catch (error) {
      console.error('카카오 로그인 실패:', error);
      Alert.alert('오류', '카카오 로그인에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // ========== WebView에서 URL 변경 감지 ==========
  const handleWebViewNavigationStateChange = async (navState) => {
    const { url } = navState;
    console.log('WebView URL 변경:', url);

    // 콜백 URL인지 확인 (카카오 로그인 완료 후 돌아오는 URL)
    if (url.includes('/api/auth/kakao/callback?code=')) {
      // WebView 닫기
      setShowWebView(false);

      // URL에서 인가 코드(code) 추출
      // 예: http://localhost:8080/api/auth/kakao/callback?code=ABC123
      const code = url.split('code=')[1]?.split('&')[0];
      console.log('인가 코드:', code);

      if (code) {
        try {
          setLoading(true);

          // 3. 인가 코드로 로그인 처리
          const result = await authApi.kakaoCallback(code);
          console.log('로그인 결과:', result);

          if (result.success) {
            // 4. 로그인 성공!
            // Context의 login 함수 호출 → 즉시 상태 변경
            await login(
              {
                userId: result.data.userId,
                email: result.data.email,
                nickname: result.data.nickname,
                profileImage: result.data.profileImage,
              },
              {
                accessToken: result.data.accessToken,
                refreshToken: result.data.refreshToken,
              }
            );
            // AppNavigator가 즉시 반응하여 HomeScreen 렌더링!
          } else {
            Alert.alert('오류', '로그인에 실패했습니다.');
          }

        } catch (error) {
          console.error('로그인 처리 실패:', error);
          Alert.alert('오류', '로그인 처리 중 문제가 발생했습니다.');
        } finally {
          setLoading(false);
        }
      }
    }
  };

  // ========== 화면 렌더링 ==========
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>

        {/* 상단: 로고/타이틀 영역 */}
        <View style={styles.header}>
          <Text style={styles.title}>BadHabitZero</Text>
          <Text style={styles.subtitle}>
            악습을 돈으로 환산하고{'\n'}더 나은 내가 되세요
          </Text>
        </View>

        {/* 하단: 로그인 버튼 */}
        <View style={styles.buttonContainer}>
          <TouchableOpacity
            style={styles.kakaoButton}
            onPress={handleKakaoLogin}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color={colors.black} />
            ) : (
              <Text style={styles.kakaoButtonText}>🗨️ 카카오로 시작하기</Text>
            )}
          </TouchableOpacity>
        </View>

      </View>

      {/* 카카오 로그인 WebView 모달 */}
      <Modal
        visible={showWebView}
        animationType="slide"
        onRequestClose={() => setShowWebView(false)}
      >
        <SafeAreaView style={styles.webViewContainer}>
          {/* 닫기 버튼 */}
          <TouchableOpacity
            style={styles.closeButton}
            onPress={() => setShowWebView(false)}
          >
            <Text style={styles.closeButtonText}>✕ 닫기</Text>
          </TouchableOpacity>

          {/* 카카오 로그인 페이지 */}
          <WebView
            source={{ uri: loginUrl }}
            // onNavigationStateChange: URL 변경 감지 이벤트 핸들러
            onNavigationStateChange={handleWebViewNavigationStateChange}
            style={styles.webView}
          />
        </SafeAreaView>
      </Modal>

    </SafeAreaView>
  );
}

// ========== 스타일 정의 ==========
// StyleSheet.create: React Native에서 스타일을 정의하는 방법
const styles = StyleSheet.create({
  container: {
    flex: 1,  // 화면 전체 차지
    backgroundColor: colors.white,
  },
  content: {
    flex: 1,
    justifyContent: 'space-between',  // 위아래로 공간 분배
    paddingHorizontal: 24,
    paddingVertical: 60,
  },
  header: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: colors.primary,
    marginBottom: 16,
  },
  subtitle: {
    fontSize: 16,
    color: colors.gray,
    textAlign: 'center',
    lineHeight: 24,
  },
  buttonContainer: {
    paddingBottom: 20,
  },
  kakaoButton: {
    backgroundColor: colors.kakao,
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  kakaoButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.black,
  },
  webViewContainer: {
    flex: 1,
    backgroundColor: colors.white,
  },
  closeButton: {
    padding: 16,
    alignItems: 'flex-end',
  },
  closeButtonText: {
    fontSize: 16,
    color: colors.gray,
  },
  webView: {
    flex: 1,
  },
});