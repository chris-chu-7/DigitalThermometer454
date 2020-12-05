package com.example.digitalthermometer;

public enum Symptoms {
    Cough,
    Headache,
    Difficulty_Breathing,
    Nausea,
    Loss_of_Taste_Smell,
    Sore_Throat,
    // Order matters here as there are dependencies on location..
    // If adding more, append here
    None;



    public static Symptoms fromInteger(int x){
        switch (x) {
            case 0:
                return Symptoms.Cough;
            case 1:
                return Symptoms.Headache;
            case 2:
                return Symptoms.Difficulty_Breathing;
            case 3:
                return Symptoms.Nausea;
            case 4:
                return Symptoms.Loss_of_Taste_Smell;
            case 5:
                return Symptoms.Sore_Throat;
            default:
                return Symptoms.None;
        }
    }
}
