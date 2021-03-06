package com.github.yeriomin.yalpstore;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.yeriomin.playstoreapi.AuthException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

abstract class GoogleApiAsyncTask extends AsyncTask<Void, Void, Throwable> {

    protected Context context;
    protected String progressDialogTitle;
    protected String progressDialogMessage;
    protected ProgressDialog dialog;
    protected TextView errorView;
    protected GoogleApiAsyncTask taskClone;

    public void setContext(Context context) {
        this.context = context;
    }

    public void prepareDialog(String message, String title) {
        this.progressDialogTitle = title;
        this.progressDialogMessage = message;
    }

    public void setErrorView(TextView errorView) {
        this.errorView = errorView;
    }

    public void setTaskClone(GoogleApiAsyncTask taskClone) {
        this.taskClone = taskClone;
    }

    @Override
    protected void onPreExecute() {
        if (null != progressDialogMessage && null != progressDialogTitle) {
            this.dialog = ProgressDialog.show(this.context, this.progressDialogTitle, this.progressDialogMessage, true);
        }
    }

    @Override
    protected void onPostExecute(Throwable e) {
        if (null != this.dialog) {
            this.dialog.dismiss();
        }
        if (e instanceof RuntimeException && null != e.getCause()) {
            e = e.getCause();
        }
        if (e instanceof AuthException) {
            if (e instanceof CredentialsEmptyException) {
                Log.w(getClass().getName(), "Credentials empty");
            } else {
                Toast.makeText(
                    this.context.getApplicationContext(),
                    this.context.getString(R.string.error_incorrect_password),
                    Toast.LENGTH_LONG
                ).show();
                new PlayStoreApiWrapper(this.context).forceTokenRefresh();
            }
            CredentialsDialogBuilder builder = new CredentialsDialogBuilder(this.context);
            builder.setTaskClone(this.taskClone);
            builder.show();
        } else if (e instanceof IOException) {
            String message;
            if (e instanceof UnknownHostException
                || e instanceof SSLHandshakeException
                || e instanceof ConnectException
                || e instanceof SocketException
                || e instanceof SocketTimeoutException) {
                message = this.context.getString(R.string.error_no_network);
            } else {
                message = this.context.getString(R.string.error_network_other, e.getClass().getName() + " " + e.getMessage());
            }
            if (null != this.errorView) {
                this.errorView.setText(message);
            } else {
                Toast.makeText(this.context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        } else if (e != null) {
            Log.e(getClass().getName(), "Unknown exception " + e.getClass().getName() + " " + e.getMessage());
            e.printStackTrace();
        }
    }
}
