<script setup>
import { ref, onMounted } from 'vue'
import { playGame, resetGame as apiResetGame } from './services/gameService'

const choices = [
  { name: 'ROCK', emoji: '✊' },
  { name: 'PAPER', emoji: '✋' },
  { name: 'SCISSORS', emoji: '✌️' }
]

const playerChoice = ref('')
const aiChoice = ref('')
const result = ref('Choose your move!')
const gameStatus = ref('') // 'PLAYER_WINS', 'AI_WINS', 'DRAW'
const playerScore = ref(0)
const aiScore = ref(0)
const currentStreak = ref(0)
const error = ref('')
const loading = ref(false)

// Reset session on page mount
onMounted(async () => {
  await handleReset()
})

async function handleReset() {
  try {
    await apiResetGame()
    playerChoice.value = ''
    aiChoice.value = ''
    result.value = 'Choose your move!'
    gameStatus.value = ''
    playerScore.value = 0
    aiScore.value = 0
    currentStreak.value = 0
    error.value = ''
  } catch (err) {
    console.error('Reset failed:', err)
  }
}

function emoji(choice) {
  return choices.find(c => c.name === choice)?.emoji || '❓'
}

async function choose(choice) {
  loading.value = true
  error.value = ''

  try {
    const data = await playGame(choice)

    playerChoice.value = data.playerChoice
    aiChoice.value = data.aiChoice
    playerScore.value = data.playerScore
    aiScore.value = data.aiScore
    currentStreak.value = data.currentStreak
    result.value = data.message
    gameStatus.value = data.result

  } catch (err) {
    console.error(err)
    error.value = 'Cannot connect to Spring Boot. Ensure backend is running.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="page">
    <div class="card">
      <header class="header">
        <h1>✊ Rock Paper Scissors ✌️</h1>
        <p>Human vs Markov AI</p>
      </header>

      <!-- SCOREBOARD -->
      <section class="scoreboard">
        <div class="score-box">
          <span class="label">YOU</span>
          <span class="val">{{ playerScore }}</span>
        </div>

        <div class="vs-badge">VS</div>

        <div class="score-box">
          <span class="label">AI 🤖</span>
          <span class="val">{{ aiScore }}</span>
        </div>
      </section>

      <!-- STREAK BANNER -->
      <div v-if="currentStreak !== 0" class="streak-tag" :class="currentStreak > 0 ? 'player-streak' : 'ai-streak'">
        <span v-if="currentStreak > 0">🔥 {{ currentStreak }} Win Streak!</span>
        <span v-else>🤖 AI is on a {{ Math.abs(currentStreak) }} Win Streak!</span>
      </div>

      <!-- BATTLEFIELD -->
      <section class="battlefield" :class="gameStatus.toLowerCase()">
        <div class="fighter">
          <div class="hand-icon">{{ emoji(playerChoice) }}</div>
          <span class="move-name">{{ playerChoice || 'Waiting...' }}</span>
        </div>

        <div class="banner">
          <p>{{ result }}</p>
        </div>

        <div class="fighter">
          <div class="hand-icon">{{ emoji(aiChoice) }}</div>
          <span class="move-name">{{ aiChoice || 'Waiting...' }}</span>
        </div>
      </section>

      <!-- CONTROLS -->
      <section class="controls">
        <h3>Choose Your Move</h3>
        <div class="btn-group">
          <button
            v-for="choice in choices"
            :key="choice.name"
            :disabled="loading"
            class="choice-btn"
            @click="choose(choice.name)"
          >
            <span class="emoji">{{ choice.emoji }}</span>
            <span class="name">{{ choice.name }}</span>
          </button>
        </div>
      </section>

      <!-- RESET BUTTON -->
      <button class="reset-btn" @click="handleReset">
        🔄 Reset Game Session
      </button>

      <!-- ERROR MESSAGE -->
      <p v-if="error" class="error-msg">
        {{ error }}
      </p>
    </div>
  </main>
</template>

<style scoped>
/* GENERAL STYLES */
.page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  color: #f8fafc;
  padding: 1rem;
}

.card {
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 2.5rem;
  max-width: 520px;
  width: 100%;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  text-align: center;
}

.header h1 {
  font-size: 1.8rem;
  margin: 0;
  font-weight: 800;
}

.header p {
  color: #94a3b8;
  margin-top: 0.25rem;
  font-size: 0.9rem;
}

/* SCOREBOARD */
.scoreboard {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin: 1.5rem 0 1rem;
  background: rgba(15, 23, 42, 0.6);
  padding: 1rem;
  border-radius: 16px;
}

.score-box {
  display: flex;
  flex-direction: column;
}

.score-box .label {
  font-size: 0.75rem;
  font-weight: 700;
  color: #94a3b8;
  letter-spacing: 0.05em;
}

.score-box .val {
  font-size: 2rem;
  font-weight: 800;
  color: #38bdf8;
}

.vs-badge {
  font-weight: 900;
  color: #64748b;
  font-size: 0.9rem;
}

/* STREAK TAG */
.streak-tag {
  display: inline-block;
  padding: 0.35rem 0.85rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.player-streak {
  background: rgba(234, 179, 8, 0.2);
  color: #fde047;
  border: 1px solid rgba(234, 179, 8, 0.4);
}

.ai-streak {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.4);
}

/* BATTLEFIELD */
.battlefield {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(15, 23, 42, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  padding: 1.25rem 1rem;
  border-radius: 16px;
  margin-bottom: 1.5rem;
  transition: all 0.3s ease;
}

.battlefield.player_wins {
  border-color: rgba(34, 197, 94, 0.5);
  box-shadow: 0 0 15px rgba(34, 197, 94, 0.15);
}

.battlefield.ai_wins {
  border-color: rgba(239, 68, 68, 0.5);
  box-shadow: 0 0 15px rgba(239, 68, 68, 0.15);
}

.fighter {
  flex: 1;
}

.hand-icon {
  font-size: 3.2rem;
  transition: transform 0.2s ease;
}

.move-name {
  display: block;
  font-size: 0.8rem;
  color: #94a3b8;
  margin-top: 0.25rem;
}

.banner {
  flex: 1.2;
  font-weight: 700;
  font-size: 0.95rem;
  padding: 0 0.5rem;
  color: #f1f5f9;
}

/* CONTROLS */
.controls h3 {
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #94a3b8;
  margin-bottom: 0.75rem;
}

.btn-group {
  display: flex;
  gap: 0.75rem;
}

.choice-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem 0.5rem;
  background: #334155;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  color: #f8fafc;
  cursor: pointer;
  transition: all 0.2s ease;
}

.choice-btn .emoji {
  font-size: 1.8rem;
  margin-bottom: 0.25rem;
}

.choice-btn .name {
  font-size: 0.75rem;
  font-weight: 700;
}

.choice-btn:hover:not(:disabled) {
  background: #0284c7;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(2, 132, 199, 0.3);
}

.choice-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* RESET BUTTON */
.reset-btn {
  margin-top: 1.5rem;
  background: transparent;
  border: none;
  color: #64748b;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s ease;
}

.reset-btn:hover {
  color: #f8fafc;
}

.error-msg {
  color: #f87171;
  font-size: 0.85rem;
  margin-top: 1rem;
}
</style>