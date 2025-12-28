import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  SafeAreaView,
  TouchableOpacity,
} from 'react-native';
import { colors } from '../constants/colors';

export default function OnboardingScreen({ navigation }) {

  // 시작하기 버튼 클릭
  const handleComplete = () => {
    navigation.replace('Home');
  };

  return (
    <SafeAreaView style={styles.container}>

      {/* ===== 메인 컨텐츠 ===== */}
      <View style={styles.content}>

        {/* 환영 이모지 */}
        <Text style={styles.emoji}>🎉</Text>

        {/* 환영 메시지 */}
        <Text style={styles.title}>환영합니다!</Text>
        <Text style={styles.description}>
          BadHabitZero와 함께{'\n'}
          악습을 극복하고 돈도 모아보세요!
        </Text>

        {/* 정보 박스 */}
        <View style={styles.infoBox}>
          <Text style={styles.infoTitle}>💡 알고 계셨나요?</Text>
          <Text style={styles.infoText}>
            악습을 고친 사람들은 평균적으로{'\n'}
            더 높은 삶의 만족도를 보고합니다.
          </Text>
        </View>

        {/* 기능 소개 */}
        <View style={styles.featureList}>
          <View style={styles.featureItem}>
            <Text style={styles.featureEmoji}>📝</Text>
            <Text style={styles.featureText}>악습을 등록하세요</Text>
          </View>
          <View style={styles.featureItem}>
            <Text style={styles.featureEmoji}>🤖</Text>
            <Text style={styles.featureText}>AI가 가치를 산정해요</Text>
          </View>
          <View style={styles.featureItem}>
            <Text style={styles.featureEmoji}>✅</Text>
            <Text style={styles.featureText}>"참았다" 버튼을 누르세요</Text>
          </View>
          <View style={styles.featureItem}>
            <Text style={styles.featureEmoji}>💰</Text>
            <Text style={styles.featureText}>금고에 돈이 쌓여요!</Text>
          </View>
        </View>

      </View>

      {/* ===== 하단 버튼 ===== */}
      <TouchableOpacity
        style={styles.button}
        onPress={handleComplete}
      >
        <Text style={styles.buttonText}>시작하기</Text>
      </TouchableOpacity>

    </SafeAreaView>
  );
}

// ========== 스타일 ==========
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.white,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 24,
  },

  // 환영 메시지
  emoji: {
    fontSize: 64,
    marginBottom: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: colors.black,
    marginBottom: 8,
  },
  description: {
    fontSize: 16,
    color: colors.gray,
    textAlign: 'center',
    lineHeight: 24,
    marginBottom: 32,
  },

  // 정보 박스
  infoBox: {
    backgroundColor: colors.background,
    padding: 20,
    borderRadius: 12,
    width: '100%',
    marginBottom: 32,
  },
  infoTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.black,
    marginBottom: 8,
  },
  infoText: {
    fontSize: 14,
    color: colors.gray,
    lineHeight: 22,
  },

  // 기능 소개
  featureList: {
    width: '100%',
  },
  featureItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  featureEmoji: {
    fontSize: 24,
    marginRight: 12,
  },
  featureText: {
    fontSize: 15,
    color: colors.black,
  },

  // 하단 버튼
  button: {
    backgroundColor: colors.primary,
    marginHorizontal: 24,
    marginBottom: 40,
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  buttonText: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.white,
  },
});