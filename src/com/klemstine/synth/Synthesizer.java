package com.klemstine.synth;

public abstract interface Synthesizer
{
  public abstract double monoOutput();

  public abstract double[] stereoOutput();

  public abstract double getAux1();

  public abstract double getAux2();
}

