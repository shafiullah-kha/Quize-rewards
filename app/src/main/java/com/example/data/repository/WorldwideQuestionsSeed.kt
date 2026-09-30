package com.example.data.repository

import com.example.data.model.QuizQuestion

object WorldwideQuestionsSeed {
    fun getInitialWorldwideQuestions(): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()

        // 1. General Knowledge
        questions.add(
            QuizQuestion(
                quizId = "gk_1",
                question = "Which planet in our solar system is known as the Red Planet?",
                options = listOf("Venus", "Mars", "Jupiter", "Saturn"),
                correctAnswer = 1,
                category = "General Knowledge",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "Mars is called the Red Planet because iron minerals in its soil oxidize (rust), making it look reddish."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "gk_2",
                question = "How many continents are there on Earth?",
                options = listOf("5", "6", "7", "8"),
                correctAnswer = 2,
                category = "General Knowledge",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "There are 7 continents: Asia, Africa, North America, South America, Antarctica, Europe, and Australia."
            )
        )

        // 2. Science
        questions.add(
            QuizQuestion(
                quizId = "sci_1",
                question = "What is the chemical symbol for Gold?",
                options = listOf("Ag", "Au", "Fe", "Pb"),
                correctAnswer = 1,
                category = "Science",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "Au comes from the Latin word for gold, 'Aurum'."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "sci_2",
                question = "Which organ in the human body produces insulin?",
                options = listOf("Liver", "Pancreas", "Kidney", "Gallbladder"),
                correctAnswer = 1,
                category = "Science",
                difficulty = "Medium",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "The pancreas produces insulin, which regulates blood glucose levels."
            )
        )

        // 3. History
        questions.add(
            QuizQuestion(
                quizId = "hist_1",
                question = "In which year did World War II officially end?",
                options = listOf("1943", "1945", "1948", "1950"),
                correctAnswer = 1,
                category = "History",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "World War II ended in 1945 following the surrender of Axis forces."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "hist_2",
                question = "Who was the first President of the United States?",
                options = listOf("Thomas Jefferson", "John Adams", "George Washington", "Benjamin Franklin"),
                correctAnswer = 2,
                category = "History",
                difficulty = "Easy",
                country = "United States",
                language = "English",
                points = 10,
                explanation = "George Washington served as the first US president from 1789 to 1797."
            )
        )

        // 4. Geography & Countries & Capitals
        questions.add(
            QuizQuestion(
                quizId = "geo_1",
                question = "What is the capital city of France?",
                options = listOf("Madrid", "Paris", "Berlin", "Rome"),
                correctAnswer = 1,
                category = "Countries & Capitals",
                difficulty = "Easy",
                country = "Europe",
                language = "English",
                points = 10,
                explanation = "Paris is the capital and largest city of France."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "geo_2",
                question = "Which is the longest river in the world?",
                options = listOf("Amazon River", "Nile River", "Yangtze River", "Mississippi River"),
                correctAnswer = 1,
                category = "Geography",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "The Nile River in northeastern Africa is generally considered the longest river in the world (approx 6,650 km)."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "geo_3",
                question = "What is the capital city of Pakistan?",
                options = listOf("Karachi", "Lahore", "Islamabad", "Rawalpindi"),
                correctAnswer = 2,
                category = "Pakistan",
                difficulty = "Easy",
                country = "Pakistan",
                language = "English",
                points = 10,
                explanation = "Islamabad is the federal capital of Pakistan, built in the 1960s to replace Karachi."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "geo_4",
                question = "What is the capital city of India?",
                options = listOf("Mumbai", "New Delhi", "Kolkata", "Bengaluru"),
                correctAnswer = 1,
                category = "India",
                difficulty = "Easy",
                country = "India",
                language = "English",
                points = 10,
                explanation = "New Delhi is the national capital of India."
            )
        )

        // 5. Sports, Football & Cricket
        questions.add(
            QuizQuestion(
                quizId = "spo_1",
                question = "Which country won the FIFA Men's World Cup in 2022?",
                options = listOf("France", "Argentina", "Brazil", "Croatia"),
                correctAnswer = 1,
                category = "Football",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "Argentina won the 2022 FIFA World Cup in Qatar, captained by Lionel Messi."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "spo_2",
                question = "In Cricket, what is the maximum number of players allowed on the pitch for the batting team at one time?",
                options = listOf("1", "2", "3", "11"),
                correctAnswer = 1,
                category = "Cricket",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "Two batsmen from the batting team are on the pitch at any time: the striker and the non-striker."
            )
        )

        // 6. Technology & Computers
        questions.add(
            QuizQuestion(
                quizId = "tech_1",
                question = "What does 'CPU' stand for in computer science?",
                options = listOf(
                    "Central Processing Unit",
                    "Computer Personal Unit",
                    "Core Power Usage",
                    "Central Power Upgrade"
                ),
                correctAnswer = 0,
                category = "Computers",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "CPU stands for Central Processing Unit, often called the brain of the computer."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "tech_2",
                question = "Which programming language was developed by JetBrains and is officially recommended for Android app development?",
                options = listOf("Swift", "Kotlin", "Dart", "Rust"),
                correctAnswer = 1,
                category = "Technology",
                difficulty = "Medium",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "Kotlin is a modern, statically typed language created by JetBrains and championed by Google for Android."
            )
        )

        // 7. Space & World Records
        questions.add(
            QuizQuestion(
                quizId = "spc_1",
                question = "Who was the first person to walk on the Moon?",
                options = listOf("Buzz Aldrin", "Yuri Gagarin", "Neil Armstrong", "Michael Collins"),
                correctAnswer = 2,
                category = "Space",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "Neil Armstrong stepped onto the lunar surface on July 20, 1969 during the Apollo 11 mission."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "wrec_1",
                question = "What is the tallest mountain above sea level on Earth?",
                options = listOf("K2", "Mount Everest", "Kangchenjunga", "Lhotse"),
                correctAnswer = 1,
                category = "World Records",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "Mount Everest in the Himalayas stands at 8,848.86 meters above sea level."
            )
        )

        // 8. Brain & Logic & Mathematics
        questions.add(
            QuizQuestion(
                quizId = "math_1",
                question = "What is the square root of 144?",
                options = listOf("10", "11", "12", "14"),
                correctAnswer = 2,
                category = "Mathematics",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "12 x 12 = 144."
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "logic_1",
                question = "If 5 machines take 5 minutes to make 5 widgets, how long would 100 machines take to make 100 widgets?",
                options = listOf("100 minutes", "5 minutes", "20 minutes", "1 minute"),
                correctAnswer = 1,
                category = "Brain & Logic",
                difficulty = "Hard",
                country = "Global",
                language = "English",
                points = 15,
                explanation = "Each machine takes 5 minutes to make 1 widget. Thus, 100 machines working in parallel produce 100 widgets in 5 minutes."
            )
        )

        // 9. Animals & Nature
        questions.add(
            QuizQuestion(
                quizId = "anim_1",
                question = "Which is the largest living mammal on Earth?",
                options = listOf("African Elephant", "Blue Whale", "Giraffe", "Colossal Squid"),
                correctAnswer = 1,
                category = "Animals",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "The Antarctic Blue Whale is the largest animal on the planet, weighing up to 400,000 pounds (200 tons)."
            )
        )

        // 10. Gaming & Trivia
        questions.add(
            QuizQuestion(
                quizId = "game_1",
                question = "In popular battle royale games like Free Fire, how are legitimate reward vouchers processed in Quiz Rewards?",
                options = listOf(
                    "Through automated diamond generators",
                    "Through authorized administrative top-up and legitimate vouchers",
                    "Through game file modifications",
                    "Through unofficial bots"
                ),
                correctAnswer = 1,
                category = "Gaming",
                difficulty = "Easy",
                country = "Global",
                language = "English",
                points = 10,
                explanation = "Quiz Rewards strictly uses authorized top-up or voucher codes managed by admins, never fake generators."
            )
        )

        // 11. Multi-Language Examples (Urdu, Hindi, Arabic)
        questions.add(
            QuizQuestion(
                quizId = "lang_urdu_1",
                question = "دنیا کا سب سے بڑا براعظم کون سا ہے؟ (Which is the largest continent in the world?)",
                options = listOf("افریقہ (Africa)", "ایشیا (Asia)", "یورپ (Europe)", "امریکہ (America)"),
                correctAnswer = 1,
                category = "Languages",
                difficulty = "Easy",
                country = "Pakistan",
                language = "Urdu",
                points = 10,
                explanation = "ایشیا رقبے اور آبادی دونوں کے لحاظ سے دنیا کا سب سے بڑا براعظم ہے۔"
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "lang_hindi_1",
                question = "भारत की राष्ट्रीय नदी कौन सी है? (Which is the national river of India?)",
                options = listOf("यमुना (Yamuna)", "गंगा (Ganga)", "गोदावरी (Godavari)", "नर्मदा (Narmada)"),
                correctAnswer = 1,
                category = "India",
                difficulty = "Easy",
                country = "India",
                language = "Hindi",
                points = 10,
                explanation = "गंगा नदी भारत की सबसे पवित्र और राष्ट्रीय नदी मानी जाती है।"
            )
        )
        questions.add(
            QuizQuestion(
                quizId = "lang_ar_1",
                question = "ما هي عاصمة المملكة العربية السعودية؟ (What is the capital of Saudi Arabia?)",
                options = listOf("جدة (Jeddah)", "الرياض (Riyadh)", "الدمام (Dammam)", "مكة المكرمة (Makkah)"),
                correctAnswer = 1,
                category = "Middle East",
                difficulty = "Easy",
                country = "Middle East",
                language = "Arabic",
                points = 10,
                explanation = "الرياض هي عاصمة المملكة العربية السعودية وأكبر مدنها."
            )
        )

        return questions
    }
}
