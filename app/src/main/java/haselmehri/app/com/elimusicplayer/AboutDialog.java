package haselmehri.app.com.elimusicplayer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutDialog extends Dialog implements View.OnClickListener {

    private final Context context;

    public AboutDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about_dialog);

        String versionName = Utilities.getVersionName(context);
        if (versionName != null && !TextUtils.isEmpty(versionName)) {
            TextView txtAboutTitle = findViewById(R.id.title_text_view);
            txtAboutTitle.setText(txtAboutTitle.getText().toString().concat(" ").concat(versionName));
        }

        Spanned policy = Html.fromHtml(context.getString(R.string.about_github_link));
        TextView txtGithubLink = findViewById(R.id.github_link_text_view);
        txtGithubLink.setText(policy);
        txtGithubLink.setMovementMethod(LinkMovementMethod.getInstance());

        policy = Html.fromHtml(context.getString(R.string.about_description));
        TextView txtDescription = findViewById(R.id.description_text_view);
        txtDescription.setText(policy);
        txtDescription.setMovementMethod(LinkMovementMethod.getInstance());

        Button btnOk = findViewById(R.id.ok_button);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View view) {

    }
}

