const API_BASE_URL = 'http://localhost:8080/api/game';

export async function playGame(choice) {
  const response = await fetch(`${API_BASE_URL}/play`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    body: JSON.stringify({ playerChoice: choice })
  });

  if (!response.ok) {
    throw new Error('Failed to play move');
  }

  return await response.json();
}

export async function resetGame() {
  const response = await fetch(`${API_BASE_URL}/reset`, {
    method: 'POST',
    credentials: 'include'
  });

  if (!response.ok) {
    throw new Error('Failed to reset game');
  }
}