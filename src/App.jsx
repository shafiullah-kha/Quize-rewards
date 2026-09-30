import React, { useState, useEffect } from 'react';

const QUIZ_QUESTIONS = [
  {
    id: 1,
    question: "What is the classic battle royale map in Free Fire?",
    options: ["Bermuda", "Erangel", "Kalahari", "Purgatory"],
    answer: 0,
    explanation: "Bermuda is the original and most popular classic map in Free Fire."
  },
  {
    id: 2,
    question: "How many points are required for the minimum 60 Diamond voucher?",
    options: ["500 Points", "1,000 Points", "2,500 Points", "5,000 Points"],
    answer: 1,
    explanation: "1,000 Points = 60 Free Fire Diamonds (Monthly redemption 5th-10th)."
  },
  {
    id: 3,
    question: "When is the monthly Free Fire diamond redemption window OPEN?",
    options: ["Every Monday", "1st to 3rd of month", "5th to 10th of each month", "End of the year"],
    answer: 2,
    explanation: "Redemption window strictly opens from the 5th to the 10th of every month."
  },
  {
    id: 4,
    question: "How many points do you earn for each successful friend referral?",
    options: ["20 Points", "50 Points", "100 Points", "250 Points"],
    answer: 2,
    explanation: "You earn +100 Points for every friend who registers with your referral code."
  },
  {
    id: 5,
    question: "What is the welcome bonus for newly registered players?",
    options: ["0 Points", "50 Points", "100 Points", "500 Points"],
    answer: 2,
    explanation: "Every new verified account receives a +100 Points welcome gift."
  }
];

export default function App() {
  const [activeTab, setActiveTab] = useState('home'); // home, quiz, spin, redeem, invite, admin
  const [points, setPoints] = useState(() => {
    const saved = localStorage.getItem('qr_points');
    return saved !== null ? parseInt(saved, 10) : 100; // starts with welcome gift
  });

  const [currentQIndex, setCurrentQIndex] = useState(0);
  const [selectedOption, setSelectedOption] = useState(null);
  const [quizFeedback, setQuizFeedback] = useState(null);
  const [quizzesAnswered, setQuizzesAnswered] = useState(0);

  // Spin Wheel State
  const [isSpinning, setIsSpinning] = useState(false);
  const [spinResult, setSpinResult] = useState(null);
  const [freeSpins, setFreeSpins] = useState(3);
  const [rotation, setRotation] = useState(0);

  // Redemption Form State
  const [playerId, setPlayerId] = useState('');
  const [diamondChoice, setDiamondChoice] = useState(60);
  const [redemptionSuccess, setRedemptionSuccess] = useState(null);
  const [redemptionError, setRedemptionError] = useState(null);

  // Referral State
  const [copiedCode, setCopiedCode] = useState(false);

  useEffect(() => {
    localStorage.setItem('qr_points', points.toString());
  }, [points]);

  // Window check: 5th to 10th of the month
  const today = new Date();
  const currentDay = today.getDate();
  const isRedemptionWindowOpen = currentDay >= 5 && currentDay <= 10;

  const handleAnswerSubmit = (optionIndex) => {
    if (selectedOption !== null) return;
    setSelectedOption(optionIndex);
    const q = QUIZ_QUESTIONS[currentQIndex];
    if (optionIndex === q.answer) {
      setQuizFeedback({ correct: true, text: "Correct! +15 Points Earned 🎉" });
      setPoints((prev) => prev + 15);
      setQuizzesAnswered((prev) => prev + 1);
    } else {
      setQuizFeedback({ correct: false, text: `Incorrect. The correct answer was: ${q.options[q.answer]}` });
    }
  };

  const handleNextQuestion = () => {
    setSelectedOption(null);
    setQuizFeedback(null);
    setCurrentQIndex((prev) => (prev + 1) % QUIZ_QUESTIONS.length);
  };

  const handleSpin = () => {
    if (isSpinning || freeSpins <= 0) return;
    setIsSpinning(true);
    setSpinResult(null);

    const outcomes = [20, 30, 40, 50, 75, 100];
    const randomIndex = Math.floor(Math.random() * outcomes.length);
    const reward = outcomes[randomIndex];
    const extraTurns = 5 * 360;
    const sliceAngle = 360 / outcomes.length;
    const targetAngle = extraTurns + (randomIndex * sliceAngle) + (sliceAngle / 2);

    setRotation((prev) => prev + targetAngle);

    setTimeout(() => {
      setIsSpinning(false);
      setSpinResult(reward);
      setPoints((prev) => prev + reward);
      setFreeSpins((prev) => Math.max(0, prev - 1));
    }, 2800);
  };

  const handleRedeem = (e) => {
    e.preventDefault();
    setRedemptionError(null);
    setRedemptionSuccess(null);

    const requiredPts = (diamondChoice / 60) * 1000;
    if (!playerId.trim()) {
      setRedemptionError("Please enter your Free Fire Player ID (UID)");
      return;
    }
    if (points < requiredPts) {
      setRedemptionError(`Insufficient points! You need ${requiredPts} Points for ${diamondChoice} Diamonds.`);
      return;
    }

    setPoints((prev) => prev - requiredPts);
    setRedemptionSuccess(`Success! Request for ${diamondChoice} Diamonds submitted for Player ID: ${playerId}. Status: PENDING review.`);
    setPlayerId('');
  };

  const copyReferralCode = () => {
    navigator.clipboard?.writeText("QB-ADM777");
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#0d1117', color: '#f0f6fc', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <header style={{
        backgroundColor: '#161b22',
        borderBottom: '1px solid #30363d',
        padding: '14px 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 50
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }} onClick={() => setActiveTab('home')}>
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '10px',
            background: 'linear-gradient(135deg, #FFD700, #FFA000)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '20px'
          }}>💎</div>
          <div>
            <div style={{ fontWeight: '800', fontSize: '18px', color: '#FFD700', letterSpacing: '0.5px' }}>Quiz Rewards</div>
            <div style={{ fontSize: '11px', color: '#8b949e' }}>Play Quizzes • Earn Diamonds</div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{
            background: 'linear-gradient(135deg, #1c2128, #2d333b)',
            border: '1px solid #FFD700',
            borderRadius: '20px',
            padding: '6px 14px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <span style={{ fontSize: '16px' }}>💰</span>
            <span style={{ fontWeight: '800', color: '#FFE082', fontSize: '15px' }}>{points.toLocaleString()}</span>
            <span style={{ fontSize: '11px', color: '#8b949e', textTransform: 'uppercase' }}>Pts</span>
          </div>

          <button
            onClick={() => setActiveTab('admin')}
            style={{
              backgroundColor: '#21262d',
              border: '1px solid #f85149',
              color: '#ff7b72',
              borderRadius: '8px',
              padding: '6px 12px',
              fontSize: '12px',
              fontWeight: '600',
              cursor: 'pointer'
            }}
          >
            🛡️ Admin
          </button>
        </div>
      </header>

      {/* Nav Tabs */}
      <nav style={{
        backgroundColor: '#161b22',
        borderBottom: '1px solid #21262d',
        display: 'flex',
        justifyContent: 'center',
        gap: '6px',
        padding: '8px 12px',
        overflowX: 'auto'
      }}>
        {[
          { key: 'home', label: '🏠 Home' },
          { key: 'quiz', label: '❓ Play Quiz' },
          { key: 'spin', label: '🎡 Spin & Earn' },
          { key: 'redeem', label: '💎 Redeem Diamonds' },
          { key: 'invite', label: '🎁 Invite & Earn' },
          { key: 'admin', label: '🛡️ Admin Portal' }
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            style={{
              padding: '8px 16px',
              borderRadius: '20px',
              border: 'none',
              backgroundColor: activeTab === tab.key ? '#FFD700' : 'transparent',
              color: activeTab === tab.key ? '#0d1117' : '#8b949e',
              fontWeight: activeTab === tab.key ? '800' : '500',
              fontSize: '13px',
              cursor: 'pointer',
              transition: 'all 0.2s',
              whiteSpace: 'nowrap'
            }}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {/* Content Area */}
      <main style={{ maxWidth: '850px', width: '100%', margin: '0 auto', padding: '24px 16px', flex: 1 }}>
        
        {/* TAB: HOME */}
        {activeTab === 'home' && (
          <div>
            {/* Hero Card */}
            <div style={{
              background: 'linear-gradient(135deg, #1c2128 0%, #2d333b 50%, #161b22 100%)',
              border: '1px solid #FFD700',
              borderRadius: '16px',
              padding: '24px',
              marginBottom: '24px',
              boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
              position: 'relative',
              overflow: 'hidden'
            }}>
              <div style={{ position: 'relative', zIndex: 2 }}>
                <span style={{ backgroundColor: '#FFD700', color: '#0d1117', fontSize: '11px', fontWeight: '800', padding: '4px 10px', borderRadius: '12px', textTransform: 'uppercase' }}>
                  Official Web Portal
                </span>
                <h1 style={{ fontSize: '26px', fontWeight: '800', margin: '12px 0 6px 0', color: '#ffffff' }}>
                  Play Quizzes. Earn Points. <span style={{ color: '#00E5FF' }}>Redeem Diamonds.</span>
                </h1>
                <p style={{ color: '#8b949e', fontSize: '14px', lineHeight: 1.5, margin: '0 0 18px 0', maxWidth: '580px' }}>
                  Join thousands of players taking Free Fire trivia quizzes, spinning the daily wheel, and redeeming verified diamond voucher codes directly into their gaming accounts.
                </p>

                <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                  <button
                    onClick={() => setActiveTab('quiz')}
                    style={{
                      background: 'linear-gradient(135deg, #FFD700, #FFA000)',
                      border: 'none',
                      color: '#0d1117',
                      fontWeight: '800',
                      padding: '10px 20px',
                      borderRadius: '10px',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    ▶️ Play Quiz (+15 Pts)
                  </button>

                  <button
                    onClick={() => setActiveTab('spin')}
                    style={{
                      backgroundColor: '#21262d',
                      border: '1px solid #00E5FF',
                      color: '#00E5FF',
                      fontWeight: '700',
                      padding: '10px 18px',
                      borderRadius: '10px',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    🎡 Spin Wheel ({freeSpins} left)
                  </button>
                </div>
              </div>
            </div>

            {/* Quick Stats Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', marginBottom: '24px' }}>
              <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '14px', padding: '18px' }}>
                <div style={{ fontSize: '12px', color: '#8b949e', textTransform: 'uppercase', fontWeight: '700' }}>Balance</div>
                <div style={{ fontSize: '24px', fontWeight: '800', color: '#FFD700', marginTop: '6px' }}>{points} Pts</div>
                <div style={{ fontSize: '12px', color: '#3fb950', marginTop: '4px' }}>💎 Worth {(points / 1000 * 60).toFixed(0)} Diamonds</div>
              </div>

              <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '14px', padding: '18px' }}>
                <div style={{ fontSize: '12px', color: '#8b949e', textTransform: 'uppercase', fontWeight: '700' }}>Redemption Window</div>
                <div style={{ fontSize: '18px', fontWeight: '800', color: isRedemptionWindowOpen ? '#3fb950' : '#ffa657', marginTop: '6px' }}>
                  {isRedemptionWindowOpen ? "🟢 OPEN NOW (5th-10th)" : "⏳ CLOSED (Opens 5th-10th)"}
                </div>
                <div style={{ fontSize: '12px', color: '#8b949e', marginTop: '4px' }}>60 Diamonds = 1,000 Points</div>
              </div>

              <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '14px', padding: '18px' }}>
                <div style={{ fontSize: '12px', color: '#8b949e', textTransform: 'uppercase', fontWeight: '700' }}>Welcome Gift</div>
                <div style={{ fontSize: '20px', fontWeight: '800', color: '#3fb950', marginTop: '6px' }}>+100 Points ✅</div>
                <div style={{ fontSize: '12px', color: '#8b949e', marginTop: '4px' }}>Credited to verified accounts</div>
              </div>
            </div>

            {/* Quick Actions Card */}
            <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '14px', padding: '20px' }}>
              <h3 style={{ margin: '0 0 14px 0', fontSize: '16px' }}>🚀 How to Earn Free Fire Diamonds</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px', backgroundColor: '#0d1117', borderRadius: '10px' }}>
                  <span style={{ fontSize: '22px' }}>1️⃣</span>
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '14px' }}>Answer 15 Daily Trivia Questions</div>
                    <div style={{ fontSize: '12px', color: '#8b949e' }}>Earn +15 points for every correct answer.</div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px', backgroundColor: '#0d1117', borderRadius: '10px' }}>
                  <span style={{ fontSize: '22px' }}>2️⃣</span>
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '14px' }}>Spin the Daily Fortune Wheel</div>
                    <div style={{ fontSize: '12px', color: '#8b949e' }}>Win up to 100 points per spin with 3 daily free spins.</div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '10px', backgroundColor: '#0d1117', borderRadius: '10px' }}>
                  <span style={{ fontSize: '22px' }}>3️⃣</span>
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '14px' }}>Invite Friends (+100 Pts Each)</div>
                    <div style={{ fontSize: '12px', color: '#8b949e' }}>Share your code to stack points faster.</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB: QUIZ */}
        {activeTab === 'quiz' && (
          <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '16px', padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <span style={{ fontSize: '13px', color: '#8b949e', fontWeight: '600' }}>
                Question {currentQIndex + 1} of {QUIZ_QUESTIONS.length}
              </span>
              <span style={{ backgroundColor: '#21262d', color: '#FFD700', fontSize: '12px', fontWeight: '700', padding: '4px 10px', borderRadius: '12px' }}>
                +15 Points
              </span>
            </div>

            <h2 style={{ fontSize: '18px', fontWeight: '700', margin: '0 0 20px 0', lineHeight: 1.4 }}>
              {QUIZ_QUESTIONS[currentQIndex].question}
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '20px' }}>
              {QUIZ_QUESTIONS[currentQIndex].options.map((opt, idx) => {
                let btnBg = '#21262d';
                let btnBorder = '#30363d';
                let btnColor = '#f0f6fc';

                if (selectedOption !== null) {
                  if (idx === QUIZ_QUESTIONS[currentQIndex].answer) {
                    btnBg = 'rgba(63, 185, 80, 0.2)';
                    btnBorder = '#3fb950';
                    btnColor = '#3fb950';
                  } else if (idx === selectedOption) {
                    btnBg = 'rgba(248, 81, 73, 0.2)';
                    btnBorder = '#f85149';
                    btnColor = '#f85149';
                  }
                }

                return (
                  <button
                    key={idx}
                    disabled={selectedOption !== null}
                    onClick={() => handleAnswerSubmit(idx)}
                    style={{
                      padding: '14px 18px',
                      borderRadius: '10px',
                      backgroundColor: btnBg,
                      border: `1px solid ${btnBorder}`,
                      color: btnColor,
                      textAlign: 'left',
                      fontSize: '14px',
                      fontWeight: '600',
                      cursor: selectedOption === null ? 'pointer' : 'default',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px',
                      transition: 'all 0.15s'
                    }}
                  >
                    <span style={{ opacity: 0.7 }}>{String.fromCharCode(65 + idx)}.</span>
                    <span>{opt}</span>
                  </button>
                );
              })}
            </div>

            {quizFeedback && (
              <div style={{
                padding: '12px 16px',
                borderRadius: '10px',
                backgroundColor: quizFeedback.correct ? 'rgba(63, 185, 80, 0.15)' : 'rgba(248, 81, 73, 0.15)',
                border: `1px solid ${quizFeedback.correct ? '#3fb950' : '#f85149'}`,
                color: quizFeedback.correct ? '#3fb950' : '#f85149',
                fontSize: '13px',
                fontWeight: '600',
                marginBottom: '16px'
              }}>
                {quizFeedback.text}
              </div>
            )}

            {selectedOption !== null && (
              <button
                onClick={handleNextQuestion}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '10px',
                  backgroundColor: '#FFD700',
                  color: '#0d1117',
                  fontWeight: '800',
                  fontSize: '14px',
                  border: 'none',
                  cursor: 'pointer'
                }}
              >
                Next Question ➔
              </button>
            )}
          </div>
        )}

        {/* TAB: SPIN */}
        {activeTab === 'spin' && (
          <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '16px', padding: '24px', textAlign: 'center' }}>
            <h2 style={{ fontSize: '22px', fontWeight: '800', color: '#FFD700', margin: '0 0 6px 0' }}>🎡 Spin & Earn Fortune Wheel</h2>
            <p style={{ color: '#8b949e', fontSize: '13px', margin: '0 0 20px 0' }}>
              Spin to earn between 20 to 100 bonus wallet points. Free spins reset daily!
            </p>

            {/* Wheel Canvas Mockup */}
            <div style={{
              width: '240px',
              height: '240px',
              margin: '0 auto 24px auto',
              borderRadius: '50%',
              border: '6px solid #FFD700',
              background: 'conic-gradient(#FFD700 0% 16%, #00E5FF 16% 33%, #FF5722 33% 50%, #4CAF50 50% 66%, #9C27B0 66% 83%, #FF9800 83% 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              position: 'relative',
              boxShadow: '0 0 30px rgba(255, 215, 0, 0.3)',
              transform: `rotate(${rotation}deg)`,
              transition: isSpinning ? 'transform 2.8s cubic-bezier(0.15, 0.9, 0.25, 1)' : 'none'
            }}>
              <div style={{
                width: '60px',
                height: '60px',
                borderRadius: '50%',
                backgroundColor: '#161b22',
                border: '3px solid #ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#FFD700',
                fontWeight: '900',
                fontSize: '18px'
              }}>
                💎
              </div>
            </div>

            {spinResult && (
              <div style={{
                padding: '12px 20px',
                borderRadius: '12px',
                backgroundColor: 'rgba(63, 185, 80, 0.2)',
                border: '1px solid #3fb950',
                color: '#3fb950',
                fontWeight: '800',
                fontSize: '16px',
                marginBottom: '16px'
              }}>
                🎉 You won +{spinResult} Points!
              </div>
            )}

            <div style={{ marginBottom: '16px', fontSize: '13px', color: '#8b949e' }}>
              Free Spins Left Today: <strong style={{ color: '#FFD700' }}>{freeSpins}</strong>
            </div>

            <button
              onClick={handleSpin}
              disabled={isSpinning || freeSpins <= 0}
              style={{
                background: freeSpins > 0 ? 'linear-gradient(135deg, #FFD700, #FFA000)' : '#30363d',
                color: freeSpins > 0 ? '#0d1117' : '#8b949e',
                border: 'none',
                borderRadius: '12px',
                padding: '14px 32px',
                fontSize: '16px',
                fontWeight: '800',
                cursor: freeSpins > 0 && !isSpinning ? 'pointer' : 'not-allowed'
              }}
            >
              {isSpinning ? "Spinning..." : freeSpins > 0 ? "SPIN NOW! 🎯" : "Daily Free Spins Finished"}
            </button>
          </div>
        )}

        {/* TAB: REDEEM */}
        {activeTab === 'redeem' && (
          <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '16px', padding: '24px' }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '12px 16px',
              borderRadius: '12px',
              backgroundColor: isRedemptionWindowOpen ? 'rgba(63, 185, 80, 0.15)' : 'rgba(255, 166, 87, 0.15)',
              border: `1px solid ${isRedemptionWindowOpen ? '#3fb950' : '#ffa657'}`,
              marginBottom: '20px'
            }}>
              <div>
                <div style={{ fontWeight: '800', color: isRedemptionWindowOpen ? '#3fb950' : '#ffa657', fontSize: '14px' }}>
                  {isRedemptionWindowOpen ? "Monthly Window is OPEN (5th–10th)" : "Monthly Window is CLOSED"}
                </div>
                <div style={{ fontSize: '12px', color: '#8b949e', marginTop: '2px' }}>
                  Requests are verified and dispatched via Garena Topup voucher PINs.
                </div>
              </div>
              <span style={{ fontSize: '24px' }}>{isRedemptionWindowOpen ? '🔓' : '🔒'}</span>
            </div>

            <form onSubmit={handleRedeem}>
              <div style={{ marginBottom: '16px' }}>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', marginBottom: '8px' }}>
                  Select Diamond Voucher Amount:
                </label>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                  {[
                    { diamonds: 60, cost: 1000 },
                    { diamonds: 120, cost: 2000 },
                    { diamonds: 310, cost: 5000 },
                    { diamonds: 520, cost: 8000 }
                  ].map((tier) => (
                    <div
                      key={tier.diamonds}
                      onClick={() => setDiamondChoice(tier.diamonds)}
                      style={{
                        padding: '12px',
                        borderRadius: '10px',
                        border: diamondChoice === tier.diamonds ? '2px solid #00E5FF' : '1px solid #30363d',
                        backgroundColor: diamondChoice === tier.diamonds ? 'rgba(0, 229, 255, 0.1)' : '#0d1117',
                        cursor: 'pointer'
                      }}
                    >
                      <div style={{ fontWeight: '800', color: '#00E5FF', fontSize: '15px' }}>{tier.diamonds} Diamonds 💎</div>
                      <div style={{ fontSize: '12px', color: '#8b949e', marginTop: '4px' }}>Cost: {tier.cost.toLocaleString()} Points</div>
                    </div>
                  ))}
                </div>
              </div>

              <div style={{ marginBottom: '16px' }}>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: '700', marginBottom: '8px' }}>
                  Your Free Fire Player ID (UID):
                </label>
                <input
                  type="text"
                  placeholder="e.g. 104829104"
                  value={playerId}
                  onChange={(e) => setPlayerId(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px 14px',
                    borderRadius: '8px',
                    border: '1px solid #30363d',
                    backgroundColor: '#0d1117',
                    color: '#ffffff',
                    fontSize: '14px',
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              {redemptionError && (
                <div style={{ padding: '10px 14px', borderRadius: '8px', backgroundColor: 'rgba(248, 81, 73, 0.15)', color: '#f85149', fontSize: '13px', marginBottom: '16px' }}>
                  {redemptionError}
                </div>
              )}

              {redemptionSuccess && (
                <div style={{ padding: '10px 14px', borderRadius: '8px', backgroundColor: 'rgba(63, 185, 80, 0.15)', color: '#3fb950', fontSize: '13px', marginBottom: '16px' }}>
                  {redemptionSuccess}
                </div>
              )}

              <button
                type="submit"
                style={{
                  width: '100%',
                  padding: '14px',
                  borderRadius: '10px',
                  backgroundColor: '#00E5FF',
                  color: '#0d1117',
                  fontWeight: '800',
                  fontSize: '15px',
                  border: 'none',
                  cursor: 'pointer'
                }}
              >
                Submit Diamond Redemption Request
              </button>
            </form>
          </div>
        )}

        {/* TAB: INVITE */}
        {activeTab === 'invite' && (
          <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '16px', padding: '24px', textAlign: 'center' }}>
            <span style={{ fontSize: '42px' }}>🎁</span>
            <h2 style={{ fontSize: '22px', fontWeight: '800', margin: '12px 0 6px 0' }}>Invite Friends & Earn Points</h2>
            <p style={{ color: '#8b949e', fontSize: '13px', maxWidth: '480px', margin: '0 auto 20px auto' }}>
              Give friends 100 points when they sign up, and earn 100 points as soon as their account is verified!
            </p>

            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '12px',
              backgroundColor: '#0d1117',
              border: '1px dashed #FFD700',
              borderRadius: '12px',
              padding: '12px 24px',
              marginBottom: '20px'
            }}>
              <span style={{ fontSize: '20px', fontWeight: '800', letterSpacing: '2px', color: '#FFD700' }}>QB-ADM777</span>
              <button
                onClick={copyReferralCode}
                style={{
                  backgroundColor: '#FFD700',
                  color: '#0d1117',
                  border: 'none',
                  borderRadius: '6px',
                  padding: '6px 14px',
                  fontSize: '12px',
                  fontWeight: '700',
                  cursor: 'pointer'
                }}
              >
                {copiedCode ? "Copied! ✅" : "Copy Code"}
              </button>
            </div>
          </div>
        )}

        {/* TAB: ADMIN */}
        {activeTab === 'admin' && (
          <div style={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '16px', padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h2 style={{ fontSize: '20px', fontWeight: '800', color: '#f85149', margin: 0 }}>🛡️ Admin Management Dashboard</h2>
              <span style={{ backgroundColor: 'rgba(248, 81, 73, 0.2)', color: '#f85149', fontSize: '11px', fontWeight: '800', padding: '4px 10px', borderRadius: '10px' }}>
                ADMIN ROLE
              </span>
            </div>

            <div style={{ backgroundColor: '#0d1117', border: '1px solid #30363d', borderRadius: '10px', padding: '14px', marginBottom: '16px' }}>
              <div style={{ fontSize: '12px', color: '#8b949e' }}>Admin Account:</div>
              <div style={{ fontSize: '14px', fontWeight: '700', color: '#ffffff', marginTop: '2px' }}>shafihu394366@gmail.com</div>
            </div>

            <h3 style={{ fontSize: '14px', color: '#8b949e', textTransform: 'uppercase', margin: '20px 0 10px 0' }}>
              Sample Pending Redemptions Queue
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {[
                { id: "REQ-FF-882194", user: "gamer_boy_99", player: "294819024", diamonds: 60, status: "PENDING" },
                { id: "REQ-FF-491024", user: "priya_sharma", player: "481920381", diamonds: 120, status: "PENDING" }
              ].map((req) => (
                <div key={req.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', backgroundColor: '#0d1117', borderRadius: '8px', border: '1px solid #21262d' }}>
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '13px' }}>{req.id} • {req.diamonds} Diamonds</div>
                    <div style={{ fontSize: '11px', color: '#8b949e' }}>Player UID: {req.player} ({req.user})</div>
                  </div>
                  <button
                    onClick={() => alert(`Voucher for ${req.diamonds} Diamonds processed!`)}
                    style={{
                      backgroundColor: '#238636',
                      color: '#ffffff',
                      border: 'none',
                      borderRadius: '6px',
                      padding: '6px 12px',
                      fontSize: '12px',
                      fontWeight: '700',
                      cursor: 'pointer'
                    }}
                  >
                    Approve & Issue Voucher
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer style={{
        backgroundColor: '#161b22',
        borderTop: '1px solid #21262d',
        padding: '16px 20px',
        textAlign: 'center',
        fontSize: '12px',
        color: '#8b949e'
      }}>
        Quiz Rewards Official Applet • Android & Web Edition • Connected to Firebase Project <code>quizrewards-ace9b</code>
      </footer>
    </div>
  );
}
