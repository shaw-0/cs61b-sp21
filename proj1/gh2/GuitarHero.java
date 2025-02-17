package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {
    public static final double CONCERT_A = 440.0;
    public static final int KEY_NUM = 37;
    public static final String KEYBOARD = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";

    public static void main(String[] args) {
        /* create two guitar strings, for concert A and C */
        GuitarString[] strings = new GuitarString[KEY_NUM];
        for (int i = 0; i < KEY_NUM; i++) {
            strings[i] = new GuitarString(CONCERT_A * Math.pow(2, (i - 24.0) / 12.0));
        }

        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                for (int i = 0; i < KEY_NUM; i++) {
                    if (key == KEYBOARD.charAt(i)) {
                        strings[i].pluck();
                    }
                }
            }

            /* compute the superposition of samples */
//            double sample = stringA.sample() + stringC.sample();
            double sample = 0.0;
            for (int i = 0; i < KEY_NUM; i++) {
                sample = sample + strings[i].sample();
            }
            sample = sample / KEY_NUM;

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
//            stringA.tic();
//            stringC.tic();
            for (int i = 0; i < KEY_NUM; i++) {
                strings[i].tic();
            }
        }
    }
}
