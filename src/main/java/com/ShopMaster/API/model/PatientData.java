package com.ShopMaster.API.model;

public class PatientData {
    public double age;                   // REAL
    public String sex;                   // {F, M}
    public String chestpaintype;         // {ASY, ATA, NAP, TA}
    public double restingbp;             // REAL
    public double cholesterol;           // REAL
    public String fastingbs;             // {0, 1} - como String porque en Weka es nominal
    public String restingecg;            // {LVH, Normal, ST}
    public double maxhr;                 // REAL
    public String exerciseangina;        // {N, Y}
    public double oldpeak;              // REAL
    public String st_slope;              // {Down, Flat, Up}

    // Getters y Setters
    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getChestpaintype() {
        return chestpaintype;
    }

    public void setChestpaintype(String chestpaintype) {
        this.chestpaintype = chestpaintype;
    }

    public double getRestingbp() {
        return restingbp;
    }

    public void setRestingbp(double restingbp) {
        this.restingbp = restingbp;
    }

    public double getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(double cholesterol) {
        this.cholesterol = cholesterol;
    }

    public String getFastingbs() {
        return fastingbs;
    }

    public void setFastingbs(String fastingbs) {
        this.fastingbs = fastingbs;
    }

    public String getRestingecg() {
        return restingecg;
    }

    public void setRestingecg(String restingecg) {
        this.restingecg = restingecg;
    }

    public double getMaxhr() {
        return maxhr;
    }

    public void setMaxhr(double maxhr) {
        this.maxhr = maxhr;
    }

    public String getExerciseangina() {
        return exerciseangina;
    }

    public void setExerciseangina(String exerciseangina) {
        this.exerciseangina = exerciseangina;
    }

    public double getOldpeak() {
        return oldpeak;
    }

    public void setOldpeak(double oldpeak) {
        this.oldpeak = oldpeak;
    }

    public String getSt_slope() {
        return st_slope;
    }

    public void setSt_slope(String st_slope) {
        this.st_slope = st_slope;
    }
}
