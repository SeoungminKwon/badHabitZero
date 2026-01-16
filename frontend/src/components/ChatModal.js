import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  Modal,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  SafeAreaView,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
} from 'react-native';
import { colors } from '../constants/colors';
import { startChatSession, sendChatMessage } from '../api/habitApi';

export default function ChatModal({ visible, onClose, onComplete, habitData }) {
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState('');
  const [sessionId, setSessionId] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [questionInfo, setQuestionInfo] = useState({ current: 0, total: 0 });
  const scrollViewRef = useRef(null);

  // 모달이 열리면 세션 시작
  useEffect(() => {
    if (visible && habitData) {
      initializeChat();
    }
  }, [visible, habitData]);

  // 새 메시지가 추가되면 스크롤 아래로
  useEffect(() => {
    if (scrollViewRef.current) {
      setTimeout(() => {
        scrollViewRef.current.scrollToEnd({ animated: true });
      }, 100);
    }
  }, [messages]);

  const initializeChat = async () => {
    setIsLoading(true);
    setMessages([]);
    setSessionId(null);

    try {
      const response = await startChatSession({
        habitName: habitData.name,
        category: habitData.category,
        reason: habitData.reason,
      });

      if (response.success) {
        setSessionId(response.data.sessionId);
        setQuestionInfo({
          current: response.data.questionNumber,
          total: response.data.totalQuestions,
        });

        // AI 첫 질문 추가
        setMessages([
          { type: 'ai', text: response.data.message, timestamp: new Date() },
        ]);
      }
    } catch (error) {
      console.error('챗봇 세션 시작 실패:', error);
      setMessages([
        { type: 'ai', text: '죄송합니다. 연결에 문제가 발생했습니다. 다시 시도해주세요.', timestamp: new Date() },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSend = async () => {
    if (!inputText.trim() || isLoading || !sessionId) return;

    const userMessage = inputText.trim();
    setInputText('');

    // 사용자 메시지 추가
    setMessages(prev => [
      ...prev,
      { type: 'user', text: userMessage, timestamp: new Date() },
    ]);

    setIsLoading(true);

    try {
      const response = await sendChatMessage(sessionId, userMessage);

      if (response.success) {
        const data = response.data;

        setQuestionInfo({
          current: data.questionNumber,
          total: data.totalQuestions,
        });

        // AI 응답 추가
        setMessages(prev => [
          ...prev,
          { type: 'ai', text: data.message, timestamp: new Date() },
        ]);

        // 대화 완료 체크 (Java boolean isComplete → JSON "complete")
        if (data.complete && data.valueResult) {
          // 잠시 후 완료 콜백 실행
          setTimeout(() => {
            onComplete(data.valueResult);
          }, 1500);
        }
      }
    } catch (error) {
      console.error('메시지 전송 실패:', error);
      setMessages(prev => [
        ...prev,
        { type: 'ai', text: '죄송합니다. 오류가 발생했습니다.', timestamp: new Date() },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setMessages([]);
    setSessionId(null);
    setInputText('');
    setQuestionInfo({ current: 0, total: 0 });
    onClose();
  };

  const renderMessage = (message, index) => {
    const isAI = message.type === 'ai';

    return (
      <View
        key={index}
        style={[
          styles.messageContainer,
          isAI ? styles.aiMessageContainer : styles.userMessageContainer,
        ]}
      >
        <View
          style={[
            styles.messageBubble,
            isAI ? styles.aiBubble : styles.userBubble,
          ]}
        >
          <Text style={[styles.messageText, isAI ? styles.aiText : styles.userText]}>
            {message.text}
          </Text>
        </View>
      </View>
    );
  };

  return (
    <Modal
      visible={visible}
      animationType="slide"
      onRequestClose={handleClose}
    >
      <SafeAreaView style={styles.container}>
        <KeyboardAvoidingView
          style={styles.keyboardView}
          behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
          {/* 헤더 */}
          <View style={styles.header}>
            <View style={styles.headerLeft}>
              <Text style={styles.headerTitle}>가치 산정</Text>
              {questionInfo.total > 0 && (
                <Text style={styles.progressText}>
                  {questionInfo.current} / {questionInfo.total}
                </Text>
              )}
            </View>
            <TouchableOpacity onPress={handleClose} style={styles.closeButton}>
              <Text style={styles.closeButtonText}>닫기</Text>
            </TouchableOpacity>
          </View>

          {/* 메시지 영역 */}
          <ScrollView
            ref={scrollViewRef}
            style={styles.messageList}
            contentContainerStyle={styles.messageListContent}
          >
            {messages.map((message, index) => renderMessage(message, index))}

            {/* 타이핑 인디케이터 */}
            {isLoading && (
              <View style={[styles.messageContainer, styles.aiMessageContainer]}>
                <View style={[styles.messageBubble, styles.aiBubble, styles.typingBubble]}>
                  <ActivityIndicator size="small" color={colors.gray} />
                  <Text style={styles.typingText}>입력 중...</Text>
                </View>
              </View>
            )}
          </ScrollView>

          {/* 입력 영역 */}
          <View style={styles.inputContainer}>
            <TextInput
              style={styles.textInput}
              value={inputText}
              onChangeText={setInputText}
              placeholder="답변을 입력하세요..."
              placeholderTextColor={colors.gray}
              multiline
              maxLength={500}
              editable={!isLoading && !!sessionId}
            />
            <TouchableOpacity
              style={[
                styles.sendButton,
                (!inputText.trim() || isLoading) && styles.sendButtonDisabled,
              ]}
              onPress={handleSend}
              disabled={!inputText.trim() || isLoading}
            >
              <Text style={styles.sendButtonText}>전송</Text>
            </TouchableOpacity>
          </View>
        </KeyboardAvoidingView>
      </SafeAreaView>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  keyboardView: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: colors.white,
    borderBottomWidth: 1,
    borderBottomColor: colors.lightGray,
  },
  headerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.black,
  },
  progressText: {
    fontSize: 14,
    color: colors.primary,
    fontWeight: '500',
  },
  closeButton: {
    padding: 8,
  },
  closeButtonText: {
    fontSize: 16,
    color: colors.gray,
  },
  messageList: {
    flex: 1,
  },
  messageListContent: {
    padding: 16,
    paddingBottom: 24,
  },
  messageContainer: {
    marginBottom: 12,
  },
  aiMessageContainer: {
    alignItems: 'flex-start',
  },
  userMessageContainer: {
    alignItems: 'flex-end',
  },
  messageBubble: {
    maxWidth: '80%',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 16,
  },
  aiBubble: {
    backgroundColor: colors.white,
    borderTopLeftRadius: 4,
  },
  userBubble: {
    backgroundColor: colors.primary,
    borderTopRightRadius: 4,
  },
  messageText: {
    fontSize: 15,
    lineHeight: 22,
  },
  aiText: {
    color: colors.black,
  },
  userText: {
    color: colors.white,
  },
  typingBubble: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  typingText: {
    fontSize: 14,
    color: colors.gray,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: colors.white,
    borderTopWidth: 1,
    borderTopColor: colors.lightGray,
    gap: 8,
  },
  textInput: {
    flex: 1,
    minHeight: 40,
    maxHeight: 100,
    paddingHorizontal: 16,
    paddingVertical: 10,
    backgroundColor: colors.background,
    borderRadius: 20,
    fontSize: 15,
    color: colors.black,
  },
  sendButton: {
    paddingHorizontal: 16,
    paddingVertical: 10,
    backgroundColor: colors.primary,
    borderRadius: 20,
  },
  sendButtonDisabled: {
    backgroundColor: colors.lightGray,
  },
  sendButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: colors.white,
  },
});
