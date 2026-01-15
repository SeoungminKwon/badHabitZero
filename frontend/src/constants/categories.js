export const CATEGORIES = [
  { key: 'SMOKING', label: '흡연', icon: '🚬' },
  { key: 'DRINKING', label: '음주', icon: '🍺' },
  { key: 'EATING', label: '과식/야식', icon: '🍕' },
  { key: 'SPENDING', label: '과소비', icon: '💸' },
  { key: 'LAZINESS', label: '게으름', icon: '😴' },
  { key: 'DIGITAL', label: '스마트폰/게임', icon: '📱' },
  { key: 'CAFFEINE', label: '카페인', icon: '☕' },
  { key: 'GAMBLING', label: '도박', icon: '🎰' },
  { key: 'OTHER', label: '기타', icon: '📌' },
];

export const getCategoryByKey = (key) => {
  return CATEGORIES.find(c => c.key === key) || { key: 'OTHER', label: '기타', icon: '📌' };
};