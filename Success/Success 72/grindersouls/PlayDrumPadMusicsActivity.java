package com.deffe.macros.grindersouls;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlayDrumPadMusicsActivity extends AppCompatActivity
{
    private MediaPlayer player,smallPlayer;
    private TextView sound1,sound2,sound3,sound4,sound5,sound6,sound7,sound8, sound9,sound10,sound11,sound12,sound13,sound14;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_drum_pad_musics);

        sound1 = findViewById(R.id.sound1);
        sound2 = findViewById(R.id.sound2);
        sound3 = findViewById(R.id.sound3);
        sound4 = findViewById(R.id.sound4);
        sound5 = findViewById(R.id.sound5);
        sound6 = findViewById(R.id.sound6);
        sound7 = findViewById(R.id.sound7);
        sound8 = findViewById(R.id.sound8);
        sound9 = findViewById(R.id.sound9);
        sound10 = findViewById(R.id.sound10);
        sound11 = findViewById(R.id.sound11);
        sound12 = findViewById(R.id.sound12);
        sound13 = findViewById(R.id.sound13);
        sound14 = findViewById(R.id.sound14);

    }

    public void playSound1 (View v)
    {
        if (player == null)
        {
            player = MediaPlayer.create(this,R.raw.sound1);
        }
        else
        {
            player.release();
            player = MediaPlayer.create(this,R.raw.sound1);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                player.start();
            }
        });

        player.start();

        sound1.setBackground(getResources().getDrawable(R.drawable.blue_button));
        sound2.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound3.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound4.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound5.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound6.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound7.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound8.setBackground(getResources().getDrawable(R.drawable.grey_button));
    }
    public void playSound2 (View v)
    {
        if (player == null)
        {
            player = MediaPlayer.create(this, R.raw.sound2);
        }
        else
        {
            player.release();
            player = MediaPlayer.create(this,R.raw.sound2);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                player.start();
            }
        });

        player.start();

        sound1.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound2.setBackground(getResources().getDrawable(R.drawable.blue_button));
        sound3.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound4.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound5.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound6.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound7.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound8.setBackground(getResources().getDrawable(R.drawable.grey_button));
    }
    public void playSound3 (View v)
    {
        if (player == null)
        {
            player = MediaPlayer.create(this, R.raw.sound3);
        }
        else
        {
            player.release();
            player = MediaPlayer.create(this,R.raw.sound3);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                player.start();
            }
        });

        player.start();

        sound1.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound2.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound3.setBackground(getResources().getDrawable(R.drawable.blue_button));
        sound4.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound5.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound6.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound7.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound8.setBackground(getResources().getDrawable(R.drawable.grey_button));

    }
    public void playSound4 (View v)
    {
        if (player == null)
        {
            player = MediaPlayer.create(this, R.raw.sound4);
        }
        else
        {
            player.release();
            player = MediaPlayer.create(this,R.raw.sound4);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                player.start();
            }
        });

        player.start();

        sound1.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound2.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound3.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound4.setBackground(getResources().getDrawable(R.drawable.blue_button));
        sound5.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound6.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound7.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound8.setBackground(getResources().getDrawable(R.drawable.grey_button));

    }
    public void playSound5 (View v)
    {
        if (player == null)
        {
            player = MediaPlayer.create(this, R.raw.sound5);
        }
        else
        {
            player.release();
            player = MediaPlayer.create(this,R.raw.sound5);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                player.start();
            }
        });

        player.start();

        sound1.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound2.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound3.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound4.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound5.setBackground(getResources().getDrawable(R.drawable.red_button));
        sound6.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound7.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound8.setBackground(getResources().getDrawable(R.drawable.grey_button));

    }
    public void playSound6 (View v)
    {
        if (player == null)
        {
            player = MediaPlayer.create(this, R.raw.sound6);
        }
        else
        {
            player.release();
            player = MediaPlayer.create(this,R.raw.sound6);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                player.start();
            }
        });

        player.start();

        sound1.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound2.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound3.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound4.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound5.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound6.setBackground(getResources().getDrawable(R.drawable.red_button));
        sound7.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound8.setBackground(getResources().getDrawable(R.drawable.grey_button));
    }
    public void playSound7 (View v)
    {
        if (player == null)
        {
            player = MediaPlayer.create(this, R.raw.sound7);
        }
        else
        {
            player.release();
            player = MediaPlayer.create(this,R.raw.sound7);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                player.start();
            }
        });

        player.start();

        sound1.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound2.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound3.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound4.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound5.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound6.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound7.setBackground(getResources().getDrawable(R.drawable.red_button));
        sound8.setBackground(getResources().getDrawable(R.drawable.grey_button));
    }
    public void playSound8 (View v)
    {
        if (player == null)
        {
            player = MediaPlayer.create(this, R.raw.sound8);
        }
        else
        {
            player.release();
            player = MediaPlayer.create(this,R.raw.sound8);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                player.start();
            }
        });

        player.start();

        sound1.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound2.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound3.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound4.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound5.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound6.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound7.setBackground(getResources().getDrawable(R.drawable.grey_button));
        sound8.setBackground(getResources().getDrawable(R.drawable.red_button));
    }

    public void playSound9 (View v)
    {
        if (smallPlayer == null)
        {
            smallPlayer = MediaPlayer.create(this, R.raw.sound9);
        }
        else
        {
            smallPlayer.release();
            smallPlayer = MediaPlayer.create(this,R.raw.sound9);
        }

        smallPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                sound9.setBackground(getResources().getDrawable(R.drawable.grey_button));
            }
        });

        smallPlayer.start();
        sound9.setBackground(getResources().getDrawable(R.drawable.green_buton));
    }

    public void playSound10 (View v)
    {
        if (smallPlayer == null)
        {
            smallPlayer = MediaPlayer.create(this, R.raw.sound10);
        }
        else
        {
            smallPlayer.release();
            smallPlayer = MediaPlayer.create(this,R.raw.sound10);
        }

        smallPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                sound10.setBackground(getResources().getDrawable(R.drawable.grey_button));
            }
        });

        smallPlayer.start();
        sound10.setBackground(getResources().getDrawable(R.drawable.orange_button));
    }

    public void playSound11 (View v)
    {
        if (smallPlayer == null)
        {
            smallPlayer = MediaPlayer.create(this, R.raw.sound11);
        }
        else
        {
            smallPlayer.release();
            smallPlayer = MediaPlayer.create(this,R.raw.sound11);
        }

        smallPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                sound11.setBackground(getResources().getDrawable(R.drawable.grey_button));
            }
        });

        smallPlayer.start();
        sound11.setBackground(getResources().getDrawable(R.drawable.purple_button));
    }

    public void playSound12 (View v)
    {
        if (smallPlayer == null)
        {
            smallPlayer = MediaPlayer.create(this, R.raw.sound12);
        }
        else
        {
            smallPlayer.release();
            smallPlayer = MediaPlayer.create(this,R.raw.sound12);
        }

        smallPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                sound12.setBackground(getResources().getDrawable(R.drawable.grey_button));
            }
        });

        smallPlayer.start();
        sound12.setBackground(getResources().getDrawable(R.drawable.green_buton));
    }

    public void playSound13 (View v)
    {
        if (smallPlayer == null)
        {
            smallPlayer = MediaPlayer.create(this, R.raw.sound13);
        }
        else
        {
            smallPlayer.release();
            smallPlayer = MediaPlayer.create(this,R.raw.sound13);
        }

        smallPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                sound13.setBackground(getResources().getDrawable(R.drawable.grey_button));
            }
        });

        smallPlayer.start();
        sound13.setBackground(getResources().getDrawable(R.drawable.orange_button));
    }

    public void playSound14 (View v)
    {
        if (smallPlayer == null)
        {
            smallPlayer = MediaPlayer.create(this, R.raw.sound14);
        }
        else
        {
            smallPlayer.release();
            smallPlayer = MediaPlayer.create(this,R.raw.sound14);
        }

        smallPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                sound14.setBackground(getResources().getDrawable(R.drawable.grey_button));
            }
        });

        smallPlayer.start();
        sound14.setBackground(getResources().getDrawable(R.drawable.purple_button));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (player != null)
        {
            player.release();
        }
        if (smallPlayer != null)
        {
            smallPlayer.release();
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if (player != null)
        {
            player.release();
        }
        if (smallPlayer != null)
        {
            smallPlayer.release();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (player != null)
        {
            player.release();
        }
        if (smallPlayer != null)
        {
            smallPlayer.release();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (player != null)
        {
            player.release();
        }
        if (smallPlayer != null)
        {
            smallPlayer.release();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (player != null)
        {
            player.release();
        }
        if (smallPlayer != null)
        {
            smallPlayer.release();
        }
    }
}
