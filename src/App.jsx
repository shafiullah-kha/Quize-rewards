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
  // 5 Bottom Navigation Options: Home, Quiz, Earn, Wallet, Profile
  const [activeTab, setActiveTab] = useState('home');
  const [points, setPoints] = useState(() => {
    const saved = localStorage.getItem('qr_points');
    return saved !== null ? parseInt(saved, 10) : 100;
  });

  const [currentQIndex, setCurrentQIndex] = useState(0);
  const [selectedOption, setSelectedOption] = useState(null);
  const [quizFeedback, setQuizFeedback] = useState(null);
  const [quizzesAnswered, setQuizzesAnswered] = useState(3);

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
  const [showAdminModal, setShowAdminModal] = useState(false);

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
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#0a0d14',
      color: '#f0f6fc',
      display: 'flex',
      flexDirection: 'column',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
    }}>
      {/* Top Bar matching Android GamingTopBar */}
      <header style={{
        backgroundColor: '#121721',
        borderBottom: '1px solid #232c3d',
        padding: '12px 18px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 50
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }} onClick={() => setActiveTab('home')}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '10px',
            background: 'linear-gradient(135deg, #FFD700, #FF9100)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '18px',
            boxShadow: '0 2px 8px rgba(255, 215, 0, 0.3)'
          }}>💎</div>
          <div>
            <div style={{ fontWeight: '900', fontSize: '17px', color: '#FFD700', letterSpacing: '0.4px' }}>Quiz Rewards</div>
            <div style={{ fontSize: '10px', color: '#8b949e' }}>Play Quizzes • Earn Diamonds</div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div
            onClick={() => setActiveTab('wallet')}
            style={{
              background: 'linear-gradient(135deg, #18202c, #242f40)',
              border: '1px solid #FFD700',
              borderRadius: '20px',
              padding: '5px 12px',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              cursor: 'pointer'
            }}
          >
            <span style={{ fontSize: '14px' }}>🪙</span>
            <span style={{ fontWeight: '800', color: '#FFE082', fontSize: '14px' }}>{points.toLocaleString()}</span>
            <span style={{ fontSize: '10px', color: '#8b949e', textTransform: 'uppercase' }}>Pts</span>
          </div>

          <span
            onClick={() => setShowAdminModal(true)}
            style={{
              backgroundColor: 'rgba(255, 87, 34, 0.15)',
              border: '1px solid #ff5722',
              color: '#ff7043',
              borderRadius: '6px',
              padding: '4px 8px',
              fontSize: '11px',
              fontWeight: '700',
              cursor: 'pointer'
            }}
          >
            🛡️ Admin
          </span>
        </div>
      </header>

      {/* Main Content Body */}
      <main style={{
        maxWidth: '680px',
        width: '100%',
        margin: '0 auto',
        padding: '16px 16px 90px 16px',
        flex: 1,
        boxSizing: 'border-box'
      }}>

        {/* 1. HOME TAB */}
        {activeTab === 'home' && (
          <div>
            {/* Hero Card */}
            <div style={{
              background: 'linear-gradient(135deg, #1a2230 0%, #263347 100%)',
              border: '1px solid rgba(255, 215, 0, 0.35)',
              borderRadius: '16px',
              padding: '20px',
              marginBottom: '16px',
              boxShadow: '0 4px 20px rgba(0,0,0,0.5)'
            }}>
              <span style={{
                backgroundColor: '#FFD700',
                color: '#0a0d14',
                fontSize: '10px',
                fontWeight: '900',
                padding: '3px 8px',
                borderRadius: '8px',
                letterSpacing: '0.5px'
              }}>
                OFFICIAL REWARDS PORTAL
              </span>
              <h2 style={{ fontSize: '22px', fontWeight: '900', margin: '10px 0 6px 0', color: '#ffffff' }}>
                Play Quizzes. Earn Points. <span style={{ color: '#00E5FF' }}>Redeem Diamonds.</span>
              </h2>
              <p style={{ color: '#94a3b8', fontSize: '13px', lineHeight: 1.5, margin: '0 0 16px 0' }}>
                Answer trivia questions, spin the fortune wheel, and exchange your points for official Free Fire diamond vouchers!
              </p>

              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  onClick={() => setActiveTab('quiz')}
                  style={{
                    flex: 1,
                    background: 'linear-gradient(135deg, #FFD700, #FFA000)',
                    border: 'none',
                    color: '#0a0d14',
                    fontWeight: '800',
                    padding: '12px',
                    borderRadius: '10px',
                    cursor: 'pointer',
                    fontSize: '13px'
                  }}
                >
                  ▶️ Play Quiz (+15 Pts)
                </button>
                <button
                  onClick={() => setActiveTab('earn')}
                  style={{
                    flex: 1,
                    backgroundColor: '#1b2432',
                    border: '1px solid #00E5FF',
                    color: '#00E5FF',
                    fontWeight: '700',
                    padding: '12px',
                    borderRadius: '10px',
                    cursor: 'pointer',
                    fontSize: '13px'
                  }}
                >
                  🎡 Spin & Earn
                </button>
              </div>
            </div>

            {/* Wallet Overview Card */}
            <div style={{
              backgroundColor: '#121721',
              border: '1px solid #232c3d',
              borderRadius: '14px',
              padding: '16px',
              marginBottom: '16px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontSize: '11px', color: '#94a3b8', fontWeight: '700', letterSpacing: '0.8px' }}>TOTAL WALLET BALANCE</div>
                  <div style={{ fontSize: '26px', fontWeight: '900', color: '#FFD700', marginTop: '2px' }}>
                    {points.toLocaleString()} <span style={{ fontSize: '14px', color: '#94a3b8' }}>pts</span>
                  </div>
                </div>
                <button
                  onClick={() => setActiveTab('wallet')}
                  style={{
                    backgroundColor: '#1b2432',
                    border: '1px solid #FFD700',
                    color: '#FFE082',
                    fontWeight: '700',
                    padding: '8px 14px',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    fontSize: '12px'
                  }}
                >
                  View Wallet ➔
                </button>
              </div>

              {/* Conversion bar: 60 Diamonds = 1000 Points */}
              <div style={{
                marginTop: '12px',
                padding: '10px',
                backgroundColor: '#18202c',
                borderRadius: '8px',
                border: '1px solid rgba(0, 229, 255, 0.3)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', fontWeight: '700' }}>
                  <span style={{ color: '#00E5FF' }}>💎 60 Diamonds = 1,000 Points</span>
                  <span style={{ color: '#FFE082' }}>Available: {Math.floor(points / 1000) * 60} 💎</span>
                </div>
                <div style={{
                  height: '6px',
                  backgroundColor: '#0a0d14',
                  borderRadius: '3px',
                  marginTop: '8px',
                  overflow: 'hidden'
                }}>
                  <div style={{
                    width: `${((points % 1000) / 1000) * 100}%`,
                    height: '100%',
                    backgroundColor: '#00E5FF',
                    borderRadius: '3px'
                  }} />
                </div>
                <div style={{ fontSize: '10px', color: '#94a3b8', marginTop: '6px' }}>
                  Progress to next 60 💎: {points % 1000} / 1,000 Points ({Math.floor(((points % 1000) / 1000) * 100)}%)
                </div>
              </div>
            </div>

            {/* Daily Activity Progress */}
            <div style={{
              backgroundColor: '#121721',
              border: '1px solid #232c3d',
              borderRadius: '14px',
              padding: '16px',
              marginBottom: '16px'
            }}>
              <div style={{ fontSize: '11px', color: '#94a3b8', fontWeight: '700', letterSpacing: '0.8px', marginBottom: '12px' }}>
                TODAY'S ACTIVITY PROGRESS
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                <span>Quizzes Answered</span>
                <span style={{ color: '#FFD700', fontWeight: '700' }}>{quizzesAnswered} / 15</span>
              </div>
              <div style={{ height: '6px', backgroundColor: '#1b2432', borderRadius: '3px', overflow: 'hidden', marginBottom: '12px' }}>
                <div style={{ width: `${(quizzesAnswered / 15) * 100}%`, height: '100%', backgroundColor: '#FFD700' }} />
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                <span>Free Spins Remaining</span>
                <span style={{ color: '#00E5FF', fontWeight: '700' }}>{freeSpins} / 3</span>
              </div>
              <div style={{ height: '6px', backgroundColor: '#1b2432', borderRadius: '3px', overflow: 'hidden' }}>
                <div style={{ width: `${(freeSpins / 3) * 100}%`, height: '100%', backgroundColor: '#00E5FF' }} />
              </div>
            </div>
          </div>
        )}

        {/* 2. QUIZ TAB */}
        {activeTab === 'quiz' && (
          <div style={{ backgroundColor: '#121721', border: '1px solid #232c3d', borderRadius: '16px', padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <span style={{ fontSize: '12px', color: '#94a3b8', fontWeight: '700' }}>
                Question {currentQIndex + 1} of {QUIZ_QUESTIONS.length}
              </span>
              <span style={{ backgroundColor: '#1b2432', color: '#FFD700', fontSize: '11px', fontWeight: '800', padding: '3px 8px', borderRadius: '8px' }}>
                +15 Points
              </span>
            </div>

            <h3 style={{ fontSize: '17px', fontWeight: '800', margin: '0 0 16px 0', lineHeight: 1.4 }}>
              {QUIZ_QUESTIONS[currentQIndex].question}
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '16px' }}>
              {QUIZ_QUESTIONS[currentQIndex].options.map((opt, idx) => {
                let btnBg = '#18202c';
                let btnBorder = '#283446';
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
                      padding: '12px 16px',
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
                      gap: '10px'
                    }}
                  >
                    <span style={{ opacity: 0.6 }}>{String.fromCharCode(65 + idx)}.</span>
                    <span>{opt}</span>
                  </button>
                );
              })}
            </div>

            {quizFeedback && (
              <div style={{
                padding: '10px 14px',
                borderRadius: '8px',
                backgroundColor: quizFeedback.correct ? 'rgba(63, 185, 80, 0.15)' : 'rgba(248, 81, 73, 0.15)',
                border: `1px solid ${quizFeedback.correct ? '#3fb950' : '#f85149'}`,
                color: quizFeedback.correct ? '#3fb950' : '#f85149',
                fontSize: '13px',
                fontWeight: '700',
                marginBottom: '14px'
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
                  color: '#0a0d14',
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

        {/* 3. EARN TAB (Spin & Earn + Watch Ads + Tasks) */}
        {activeTab === 'earn' && (
          <div>
            {/* Fortune Spin Wheel Card */}
            <div style={{
              backgroundColor: '#121721',
              border: '1px solid #232c3d',
              borderRadius: '16px',
              padding: '20px',
              textAlign: 'center',
              marginBottom: '16px'
            }}>
              <h3 style={{ fontSize: '18px', fontWeight: '900', color: '#FFD700', margin: '0 0 4px 0' }}>🎡 Spin & Earn Fortune Wheel</h3>
              <p style={{ color: '#94a3b8', fontSize: '12px', margin: '0 0 16px 0' }}>
                Win 20 to 100 points per spin! 3 free spins every day.
              </p>

              {/* Wheel graphic */}
              <div style={{
                width: '180px',
                height: '180px',
                margin: '0 auto 16px auto',
                borderRadius: '50%',
                border: '5px solid #FFD700',
                background: 'conic-gradient(#FFD700 0% 16%, #00E5FF 16% 33%, #FF5722 33% 50%, #4CAF50 50% 66%, #9C27B0 66% 83%, #FF9800 83% 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                transform: `rotate(${rotation}deg)`,
                transition: isSpinning ? 'transform 2.8s cubic-bezier(0.15, 0.9, 0.25, 1)' : 'none'
              }}>
                <div style={{
                  width: '46px',
                  height: '46px',
                  borderRadius: '50%',
                  backgroundColor: '#121721',
                  border: '2px solid #ffffff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '16px'
                }}>
                  🎯
                </div>
              </div>

              {spinResult && (
                <div style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  backgroundColor: 'rgba(63, 185, 80, 0.2)',
                  border: '1px solid #3fb950',
                  color: '#3fb950',
                  fontWeight: '800',
                  fontSize: '14px',
                  marginBottom: '12px'
                }}>
                  🎉 +{spinResult} Points Added to Wallet!
                </div>
              )}

              <div style={{ fontSize: '12px', color: '#94a3b8', marginBottom: '14px' }}>
                Free Spins Left: <strong style={{ color: '#FFD700' }}>{freeSpins}</strong>
              </div>

              <button
                onClick={handleSpin}
                disabled={isSpinning || freeSpins <= 0}
                style={{
                  background: freeSpins > 0 ? 'linear-gradient(135deg, #FFD700, #FFA000)' : '#283446',
                  color: freeSpins > 0 ? '#0a0d14' : '#64748b',
                  border: 'none',
                  borderRadius: '10px',
                  padding: '12px 28px',
                  fontSize: '14px',
                  fontWeight: '800',
                  cursor: freeSpins > 0 && !isSpinning ? 'pointer' : 'not-allowed'
                }}
              >
                {isSpinning ? "Spinning..." : freeSpins > 0 ? "SPIN NOW! 🎯" : "Free Spins Used for Today"}
              </button>
            </div>

            {/* Watch Ad Bonus Mockup */}
            <div style={{
              backgroundColor: '#121721',
              border: '1px solid #232c3d',
              borderRadius: '14px',
              padding: '16px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: '16px'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <span style={{ fontSize: '24px' }}>📺</span>
                <div>
                  <div style={{ fontWeight: '800', fontSize: '14px' }}>Watch Bonus Ad</div>
                  <div style={{ fontSize: '11px', color: '#94a3b8' }}>Get +1 Extra Spin or +10 Points</div>
                </div>
              </div>
              <button
                onClick={() => {
                  setPoints((p) => p + 10);
                  alert("Ad watched! +10 Points awarded.");
                }}
                style={{
                  backgroundColor: '#1b2432',
                  border: '1px solid #00E5FF',
                  color: '#00E5FF',
                  fontWeight: '700',
                  padding: '8px 14px',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontSize: '12px'
                }}
              >
                Watch (+10 Pts)
              </button>
            </div>
          </div>
        )}

        {/* 4. WALLET TAB (Diamonds Redemption + Window Rules + Form) */}
        {activeTab === 'wallet' && (
          <div>
            {/* Window Banner */}
            <div style={{
              padding: '12px 16px',
              borderRadius: '12px',
              backgroundColor: isRedemptionWindowOpen ? 'rgba(63, 185, 80, 0.15)' : 'rgba(255, 166, 87, 0.15)',
              border: `1px solid ${isRedemptionWindowOpen ? '#3fb950' : '#ffa657'}`,
              marginBottom: '16px',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <div>
                <div style={{ fontWeight: '800', color: isRedemptionWindowOpen ? '#3fb950' : '#ffa657', fontSize: '13px' }}>
                  {isRedemptionWindowOpen ? "🟢 Monthly Redemption Window: OPEN" : "⏳ Monthly Redemption Window: CLOSED"}
                </div>
                <div style={{ fontSize: '11px', color: '#94a3b8', marginTop: '2px' }}>
                  Redemptions process between the 5th and 10th of every month. Points never expire!
                </div>
              </div>
              <span style={{ fontSize: '20px' }}>{isRedemptionWindowOpen ? '🔓' : '🔒'}</span>
            </div>

            {/* Redemption Form */}
            <div style={{ backgroundColor: '#121721', border: '1px solid #232c3d', borderRadius: '14px', padding: '18px' }}>
              <h3 style={{ fontSize: '16px', fontWeight: '800', margin: '0 0 12px 0' }}>💎 Redeem Free Fire Diamonds</h3>

              <form onSubmit={handleRedeem}>
                <div style={{ marginBottom: '14px' }}>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', marginBottom: '8px' }}>
                    Select Voucher Package:
                  </label>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
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
                          padding: '10px',
                          borderRadius: '8px',
                          border: diamondChoice === tier.diamonds ? '2px solid #00E5FF' : '1px solid #283446',
                          backgroundColor: diamondChoice === tier.diamonds ? 'rgba(0, 229, 255, 0.1)' : '#18202c',
                          cursor: 'pointer'
                        }}
                      >
                        <div style={{ fontWeight: '800', color: '#00E5FF', fontSize: '14px' }}>{tier.diamonds} Diamonds 💎</div>
                        <div style={{ fontSize: '11px', color: '#94a3b8' }}>{tier.cost.toLocaleString()} Points</div>
                      </div>
                    ))}
                  </div>
                </div>

                <div style={{ marginBottom: '14px' }}>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: '700', marginBottom: '6px' }}>
                    Player UID (Game ID):
                  </label>
                  <input
                    type="text"
                    placeholder="Enter 8-10 digit Player ID"
                    value={playerId}
                    onChange={(e) => setPlayerId(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      borderRadius: '8px',
                      border: '1px solid #283446',
                      backgroundColor: '#0a0d14',
                      color: '#ffffff',
                      fontSize: '13px',
                      boxSizing: 'border-box'
                    }}
                  />
                </div>

                {redemptionError && (
                  <div style={{ padding: '8px 12px', borderRadius: '6px', backgroundColor: 'rgba(248, 81, 73, 0.15)', color: '#f85149', fontSize: '12px', marginBottom: '12px' }}>
                    {redemptionError}
                  </div>
                )}

                {redemptionSuccess && (
                  <div style={{ padding: '8px 12px', borderRadius: '6px', backgroundColor: 'rgba(63, 185, 80, 0.15)', color: '#3fb950', fontSize: '12px', marginBottom: '12px' }}>
                    {redemptionSuccess}
                  </div>
                )}

                <button
                  type="submit"
                  style={{
                    width: '100%',
                    padding: '12px',
                    borderRadius: '8px',
                    backgroundColor: '#00E5FF',
                    color: '#0a0d14',
                    fontWeight: '800',
                    fontSize: '14px',
                    border: 'none',
                    cursor: 'pointer'
                  }}
                >
                  Submit Redemption Request
                </button>
              </form>
            </div>
          </div>
        )}

        {/* 5. PROFILE TAB */}
        {activeTab === 'profile' && (
          <div>
            <div style={{
              backgroundColor: '#121721',
              border: '1px solid #232c3d',
              borderRadius: '16px',
              padding: '20px',
              marginBottom: '16px'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '16px' }}>
                <div style={{
                  width: '56px',
                  height: '56px',
                  borderRadius: '50%',
                  background: 'linear-gradient(135deg, #FFD700, #FF6D00)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '26px'
                }}>
                  👤
                </div>
                <div>
                  <div style={{ fontWeight: '800', fontSize: '18px' }}>Admin Shafihu</div>
                  <div style={{ fontSize: '12px', color: '#94a3b8' }}>shafihu394366@gmail.com</div>
                  <span style={{
                    display: 'inline-block',
                    backgroundColor: 'rgba(255, 87, 34, 0.2)',
                    color: '#ff7043',
                    fontSize: '10px',
                    fontWeight: '800',
                    padding: '2px 8px',
                    borderRadius: '6px',
                    marginTop: '4px'
                  }}>
                    ADMINISTRATOR
                  </span>
                </div>
              </div>

              {/* Referral Box */}
              <div style={{
                backgroundColor: '#18202c',
                border: '1px dashed #FFD700',
                borderRadius: '10px',
                padding: '12px 16px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '16px'
              }}>
                <div>
                  <div style={{ fontSize: '11px', color: '#94a3b8' }}>Your Referral Code (+100 Pts):</div>
                  <div style={{ fontWeight: '900', color: '#FFD700', fontSize: '16px', letterSpacing: '1px' }}>QB-ADM777</div>
                </div>
                <button
                  onClick={copyReferralCode}
                  style={{
                    backgroundColor: '#FFD700',
                    color: '#0a0d14',
                    border: 'none',
                    borderRadius: '6px',
                    padding: '6px 12px',
                    fontSize: '11px',
                    fontWeight: '800',
                    cursor: 'pointer'
                  }}
                >
                  {copiedCode ? "Copied! ✅" : "Copy Code"}
                </button>
              </div>

              {/* Admin Panel button */}
              <button
                onClick={() => setShowAdminModal(true)}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '10px',
                  backgroundColor: 'rgba(255, 87, 34, 0.15)',
                  border: '1px solid #ff5722',
                  color: '#ff7043',
                  fontWeight: '800',
                  fontSize: '13px',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '8px'
                }}
              >
                <span>🛡️</span> Open Admin Dashboard
              </button>
            </div>
          </div>
        )}

      </main>

      {/* 5 BOTTOM NAVIGATION TABS matching the Android App */}
      <nav style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        backgroundColor: '#121721',
        borderTop: '1px solid #232c3d',
        display: 'flex',
        justifyContent: 'space-around',
        padding: '8px 4px',
        zIndex: 100,
        boxShadow: '0 -4px 16px rgba(0,0,0,0.4)'
      }}>
        {[
          { key: 'home', label: 'Home', icon: '🏠' },
          { key: 'quiz', label: 'Quiz', icon: '❓' },
          { key: 'earn', label: 'Earn', icon: '🎁' },
          { key: 'wallet', label: 'Wallet', icon: '💰' },
          { key: 'profile', label: 'Profile', icon: '👤' }
        ].map((item) => {
          const isSelected = activeTab === item.key;
          return (
            <button
              key={item.key}
              onClick={() => setActiveTab(item.key)}
              style={{
                flex: 1,
                maxWidth: '90px',
                background: 'none',
                border: 'none',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '4px',
                cursor: 'pointer',
                padding: '4px 0',
                color: isSelected ? '#FFD700' : '#94a3b8',
                transition: 'color 0.15s ease'
              }}
            >
              <span style={{ fontSize: '18px' }}>{item.icon}</span>
              <span style={{ fontSize: '11px', fontWeight: isSelected ? '800' : '500' }}>{item.label}</span>
            </button>
          );
        })}
      </nav>

      {/* Admin Modal */}
      {showAdminModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.75)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 200,
          padding: '16px'
        }}>
          <div style={{
            maxWidth: '500px',
            width: '100%',
            backgroundColor: '#121721',
            border: '1px solid #ff5722',
            borderRadius: '16px',
            padding: '20px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <h3 style={{ margin: 0, color: '#ff7043' }}>🛡️ Admin Quick Control</h3>
              <button
                onClick={() => setShowAdminModal(false)}
                style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: '18px', cursor: 'pointer' }}
              >
                ✕
              </button>
            </div>
            <p style={{ fontSize: '12px', color: '#94a3b8', margin: '0 0 14px 0' }}>
              Logged in as: <strong>shafihu394366@gmail.com</strong>
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div style={{ padding: '10px', backgroundColor: '#18202c', borderRadius: '8px', fontSize: '12px' }}>
                <strong>REQ-FF-882194</strong>: 60 Diamonds for Player ID 294819024 (PENDING)
              </div>
              <div style={{ padding: '10px', backgroundColor: '#18202c', borderRadius: '8px', fontSize: '12px' }}>
                <strong>REQ-FF-491024</strong>: 120 Diamonds for Player ID 481920381 (PENDING)
              </div>
            </div>
            <button
              onClick={() => setShowAdminModal(false)}
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '8px',
                backgroundColor: '#283446',
                color: '#ffffff',
                border: 'none',
                fontWeight: '700',
                marginTop: '16px',
                cursor: 'pointer'
              }}
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
