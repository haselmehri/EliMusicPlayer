package haselmehri.app.com.elimusicplayer.model;

public class MusicPlayerSetting {
    public boolean isShowAudioVisualizer() {
        return showAudioVisualizer;
    }

    public void setShowAudioVisualizer(boolean showAudioVisualizer) {
        this.showAudioVisualizer = showAudioVisualizer;
    }

    public AudioVisualizerTypes getAudioVisualizerType() {
        return AudioVisualizerType;
    }

    public void setAudioVisualizerType(AudioVisualizerTypes audioVisualizerType) {
        AudioVisualizerType = audioVisualizerType;
    }

    public enum AudioVisualizerTypes
    {
        LineVisualizer,
        BarVisualizer,
        CircleVisualizer,
        CircleBarVisualizer,
        LineBarVisualizer
    }

    private boolean showAudioVisualizer = false;
    private AudioVisualizerTypes AudioVisualizerType = MusicPlayerSetting.AudioVisualizerTypes.BarVisualizer;
}
