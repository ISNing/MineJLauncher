package org.exthmui.minejlauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.preference.PreferenceManager;

import android.text.SpannableString;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.exthmui.minejlauncher.controller.ComponentController;
import org.exthmui.minejlauncher.controller.ComponentService;
import org.exthmui.minejlauncher.misc.StringGenerator;
import org.exthmui.minejlauncher.misc.Utils;
import org.exthmui.minejlauncher.model.Component;
import org.exthmui.minejlauncher.model.ComponentInfo;
import org.exthmui.minejlauncher.model.ComponentStatus;
import org.exthmui.minejlauncher.model.FileBaseInfo;
import org.exthmui.minejlauncher.ui.game_dl.GameDlFragment;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;

public class GameDlAdapter extends RecyclerView.Adapter<GameDlAdapter.ViewHolder> {

    private static final String TAG = "GameDlAdapter";

    private List<String> mDownloadIds;
    //private String mSelectedDownload;
    private ComponentController mDownloadController;
    private MainActivity mActivity;

    private enum Action {
        DOWNLOAD,
        PAUSE,
        RESUME,
        //INSTALL,
        //INFO,
        //DELETE,
        REMOVE,
        //CANCEL_INSTALLATION,
        //REBOOT,
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageButton mAction;
        private ImageButton mShowChangelog;
        private boolean mBtnReset;

        //private TextView mBuildDate;
        private TextView mBuildVersion;
        private TextView mBuildSize;

        private RelativeLayout mChangelogLayout;
        private TextView mChangelog;

        private ProgressBar mProgressBar;
        private TextView mProgressText;


        public ViewHolder(final View view) {
            super(view);
            mAction = (ImageButton) view.findViewById(R.id.file_action);
            mShowChangelog = (ImageButton) view.findViewById(R.id.show_introduction);

            mBtnReset = false;

            //mBuildDate = (TextView) view.findViewById(R.id.build_date);
            mBuildVersion = (TextView) view.findViewById(R.id.build_version);
            mBuildSize = (TextView) view.findViewById(R.id.build_size);


            mChangelogLayout = view.findViewById(R.id.introduction_layout);
            mChangelog = view.findViewById(R.id.introduction);
            mChangelogLayout.setVisibility(View.GONE);

            mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            mProgressText = (TextView) view.findViewById(R.id.progress_text);
        }
    }

    public GameDlAdapter(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.game_dl_item_view, viewGroup, false);
        return new ViewHolder(view);
    }

    public void setDownloadController(ComponentController downloadController) {
        mDownloadController = downloadController;
        notifyDataSetChanged();
    }

    private void handleActiveStatus(ViewHolder viewHolder, ComponentInfo component) {
        //boolean canDelete = false;

        final String downloadId = component.getId();
        if (mDownloadController.isDownloading(downloadId)) {
            //canDelete = true;
            String downloaded = StringGenerator.bytesToMegabytes(mActivity,
                    component.getFile().length());
            String total = Formatter.formatShortFileSize(mActivity, component.getFileSize());
            String percentage = NumberFormat.getPercentInstance().format(
                    component.getProgress() / 100.f);
            long eta = component.getEta();
            if (eta > 0) {
                CharSequence etaString = StringGenerator.formatETA(mActivity, eta * 1000);
                viewHolder.mProgressText.setText(mActivity.getString(
                        R.string.list_download_progress_eta_new, downloaded, total, etaString,
                        percentage));
            } else {
                viewHolder.mProgressText.setText(mActivity.getString(
                        R.string.list_download_progress_new, downloaded, total, percentage));
            }
            setButtonAction(viewHolder.mAction, Action.PAUSE, downloadId, true);
            viewHolder.mProgressBar.setIndeterminate(component.getStatus() == ComponentStatus.STARTING);
            viewHolder.mProgressBar.setProgress(component.getProgress());
        } /*else if (mDownloadController.isInstallingComponent(downloadId)) {
            setButtonAction(viewHolder.mAction, Action.CANCEL_INSTALLATION, downloadId, true);
            viewHolder.mProgressText.setText(component.getProgressText());
            viewHolder.mProgressBar.setIndeterminate(false);
            viewHolder.mProgressBar.setProgress(component.getInstallProgress());
        }*/ /*else if (mDownloadController.isVerifyingComponent(downloadId)) {
            setButtonAction(viewHolder.mAction, Action.INSTALL, downloadId, false);
            viewHolder.mProgressText.setText(R.string.list_verifying_component);
            viewHolder.mProgressBar.setIndeterminate(true);
        }*/ else {
            //canDelete = true;
            setButtonAction(viewHolder.mAction, Action.RESUME, downloadId, !isBusy());
            String downloaded = StringGenerator.bytesToMegabytes(mActivity,
                    component.getFile().length());
            String total = Formatter.formatShortFileSize(mActivity, component.getFileSize());
            String percentage = NumberFormat.getPercentInstance().format(
                    component.getProgress() / 100.f);
            viewHolder.mProgressText.setText(mActivity.getString(R.string.list_download_progress_new,
                    downloaded, total, percentage));
            viewHolder.mProgressBar.setIndeterminate(false);
            viewHolder.mProgressBar.setProgress(component.getProgress());
        }

        /*viewHolder.itemView.setOnLongClickListener(getLongClickListener(component, canDelete,
                viewHolder.mBuildDate));*/
        viewHolder.mProgressBar.setVisibility(View.VISIBLE);
        viewHolder.mProgressText.setVisibility(View.VISIBLE);
        viewHolder.mBuildSize.setVisibility(View.INVISIBLE);
    }

    private void handleNotActiveStatus(ViewHolder viewHolder, ComponentInfo component) {
        final String downloadId = component.getId();
        if (component.getStatus() == ComponentStatus.INSTALLED) {
            /*viewHolder.itemView.setOnLongClickListener(
                    getLongClickListener(component, true, viewHolder.mBuildDate));*/
            setButtonAction(viewHolder.mAction, Action.REMOVE,
                    downloadId, !isBusy());
        } /*else if (!Utils.canInstall(component)) {
            viewHolder.itemView.setOnLongClickListener(
                    getLongClickListener(component, false, viewHolder.mBuildDate));
            setButtonAction(viewHolder.mAction, Action.INFO, downloadId, !isBusy());
        }*/ else {
            /*viewHolder.itemView.setOnLongClickListener(
                    getLongClickListener(component, false, viewHolder.mBuildDate));*/
            setButtonAction(viewHolder.mAction, Action.DOWNLOAD, downloadId, !isBusy());
        }
        String fileSize = Formatter.formatShortFileSize(mActivity, component.getFileSize());
        viewHolder.mBuildSize.setText(fileSize);

        viewHolder.mProgressBar.setVisibility(View.INVISIBLE);
        viewHolder.mProgressText.setVisibility(View.INVISIBLE);
        viewHolder.mBuildSize.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        if (mDownloadIds == null) {
            viewHolder.mAction.setEnabled(false);
            return;
        }

        final String downloadId = mDownloadIds.get(i);
        ComponentInfo component = mDownloadController.getComponent(downloadId);
        if (component == null) {
            // The component was deleted
            viewHolder.mAction.setEnabled(false);
            viewHolder.mAction.setImageResource(R.drawable.ic_file_download);
            return;
        }

        //viewHolder.itemView.setSelected(downloadId.equals(mSelectedDownload));

        boolean activeLayout;
        switch (component.getPersistentStatus()) {
            case ComponentStatus.Persistent.UNKNOWN:
                activeLayout = component.getStatus() == ComponentStatus.STARTING;
                break;
            case ComponentStatus.Persistent.VERIFIED:
                activeLayout = component.getStatus() == ComponentStatus.INSTALLING;
                break;
            case ComponentStatus.Persistent.INCOMPLETE:
                activeLayout = true;
                break;
            default:
                throw new RuntimeException("Unknown component status");
        }

        /*String buildDate = StringGenerator.getDateLocalizedUTC(mActivity,
                DateFormat.LONG, component.getTimestamp());*/
        String buildVersion = component.getName();
        /*viewHolder.mBuildDate.setText(buildDate);*/
        viewHolder.mBuildVersion.setText(buildVersion);
        viewHolder.mBuildVersion.setCompoundDrawables(null, null, null, null);

        viewHolder.mShowChangelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RotateAnimation animation;
                animation = new RotateAnimation(viewHolder.mBtnReset ? 180 : 0, viewHolder.mBtnReset ? 360 : 180, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setInterpolator(new LinearInterpolator());
                animation.setDuration(200);
                //设置动画结束后保留结束状态
                animation.setFillAfter(true);
                viewHolder.mShowChangelog.startAnimation(animation);
                viewHolder.mBtnReset = !viewHolder.mBtnReset;

                int changelogVisibility = View.GONE;
                viewHolder.mShowChangelog.setAnimation(animation);
                animation.start();
                if (viewHolder.mChangelogLayout.getVisibility() == View.VISIBLE) {
                    changelogVisibility = View.GONE;
                } else if (viewHolder.mChangelogLayout.getVisibility() == View.GONE) {
                    changelogVisibility = View.VISIBLE;
                }
                viewHolder.mChangelog.setText(component.getIntroduction());
                viewHolder.mChangelogLayout.setVisibility(changelogVisibility);
            }
        });
        if (activeLayout) {
            handleActiveStatus(viewHolder, component);
        } else {
            handleNotActiveStatus(viewHolder, component);
        }
    }

    @Override
    public int getItemCount() {
        return mDownloadIds == null ? 0 : mDownloadIds.size();
    }

    public void setData(List<String> downloadIds) {
        mDownloadIds = downloadIds;
    }

    public void notifyItemChanged(String downloadId) {
        if (mDownloadIds == null) {
            return;
        }
        notifyItemChanged(mDownloadIds.indexOf(downloadId));
    }

    public void removeItem(String downloadId) {
        if (mDownloadIds == null) {
            return;
        }
        int position = mDownloadIds.indexOf(downloadId);
        mDownloadIds.remove(downloadId);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    private void startDownloadWithWarning(final String downloadId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean warn = preferences.getBoolean(Constants.PREF_MOBILE_DATA_WARNING, true);
        //if (!(Utils.getNetworkType(mActivity) == "wifi") || !warn) {
            mDownloadController.startDownload(downloadId);
            return;
        //}

        /*View checkboxView = LayoutInflater.from(mActivity).inflate(R.layout.checkbox_view, null);
        CheckBox checkbox = (CheckBox) checkboxView.findViewById(R.id.checkbox);
        checkbox.setText(R.string.checkbox_mobile_data_warning);*/

        /*new AlertDialog.Builder(mActivity)
                .setTitle(R.string.component_on_mobile_data_title)
                .setMessage(R.string.component_on_mobile_data_message)
                .setView(checkboxView)
                .setPositiveButton(R.string.action_download,
                        (dialog, which) -> {
                            if (checkbox.isChecked()) {
                                preferences.edit()
                                        .putBoolean(Constants.PREF_MOBILE_DATA_WARNING, false)
                                        .apply();
                                mActivity.supportInvalidateOptionsMenu();
                            }
                            mDownloadController.startDownload(downloadId);
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();*/
    }

    private void setButtonAction(ImageButton button, Action action, final String downloadId,
                                 boolean enabled) {
        final View.OnClickListener clickListener;
        switch (action) {
            case DOWNLOAD:
                button.setImageResource(R.drawable.ic_file_download);
                button.setEnabled(enabled);
                clickListener = enabled ? view -> startDownloadWithWarning(downloadId) : null;
                break;
            case PAUSE:
                button.setImageResource(R.drawable.ic_pause);
                button.setEnabled(enabled);
                clickListener = enabled ? view -> mDownloadController.pauseDownload(downloadId)
                        : null;
                break;
            case RESUME: {
                button.setImageResource(R.drawable.ic_play_arrow);
                button.setEnabled(enabled);
                ComponentInfo component = mDownloadController.getComponent(downloadId);
                final boolean canInstall = Utils.canInstall(component) ||
                        component.getFile().length() == component.getFileSize();
                clickListener = enabled ? view -> {
                    //if (canInstall) {
                        mDownloadController.resumeDownload(downloadId);
                    /*} else {
                        mActivity.showSnackbar(R.string.snack_component_not_installable,
                                Snackbar.LENGTH_LONG);
                    }*/
                } : null;
            }
            break;
            /*case INSTALL: {
                button.setImageResource(R.drawable.ic_archive);
                button.setEnabled(enabled);
                ComponentInfo component = mDownloadController.getComponent(downloadId);
                final boolean canInstall = Utils.canInstall(component);
                clickListener = enabled ? view -> {
                    if (canInstall) {
                        getInstallDialog(downloadId).show();
                    } else {
                        mActivity.showSnackbar(R.string.snack_component_not_installable,
                                Snackbar.LENGTH_LONG);
                    }
                } : null;
            }
            break;*/
            /*case INFO: {
                button.setImageResource(R.drawable.ic_info);
                button.setEnabled(enabled);
                clickListener = enabled ? view -> showInfoDialog() : null;
            }
            break;*/
            /*case DELETE: {
                button.setImageResource(R.drawable.ic_delete_forever);
                button.setEnabled(enabled);
                clickListener = enabled ? view -> getDeleteDialog(downloadId).show() : null;
            }
            break;*/
            case REMOVE: {
                button.setImageResource(R.drawable.ic_delete_forever);
                button.setEnabled(enabled);
                clickListener = enabled ? view -> getRemoveDialog(downloadId).show() : null;
            }
            break;
            /*case CANCEL_INSTALLATION: {
                button.setImageResource(R.drawable.ic_cancel);
                button.setEnabled(enabled);
                clickListener = enabled ? view -> getCancelInstallationDialog().show() : null;
            }
            break;*/
            default:
                clickListener = null;
        }
        //button.setAlpha(enabled ? 1.f : mAlphaDisabledValue);

        // Disable action mode when a button is clicked
        button.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(v);
            }
        });
    }

    private boolean isBusy() {
        return mDownloadController.hasActiveDownloads() || mDownloadController.isVerifyingComponent()
                || mDownloadController.isInstallingComponent();
    }

    /*private AlertDialog.Builder getDeleteDialog(final String downloadId) {
        return new AlertDialog.Builder(mActivity)
                .setTitle(R.string.confirm_remove_dialog_title)
                .setMessage(R.string.confirm_remove_dialog_message)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            mDownloadController.deleteComponent(downloadId);
                        })
                .setNegativeButton(android.R.string.cancel, null);
    }*/

    private AlertDialog.Builder getRemoveDialog(final String downloadId) {
        return new AlertDialog.Builder(mActivity)
                .setTitle(R.string.confirm_remove_dialog_title)
                .setMessage(R.string.confirm_remove_dialog_message)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            mDownloadController.removeComponent(downloadId);
                        })
                .setNegativeButton(android.R.string.cancel, null);
    }

    /*private View.OnLongClickListener getLongClickListener(final ComponentInfo component,
                                                          final boolean canDelete, View anchor) {
        return view -> {
            startActionMode(component, canDelete, anchor);
            return true;
        };
    }*/

    /*private AlertDialog.Builder getInstallDialog(final String downloadId) {
        ComponentInfo component = mDownloadController.getComponent(downloadId);

        String buildDate = StringGenerator.getDateLocalizedUTC(mActivity,
                DateFormat.MEDIUM, component.getTimestamp());
        String buildInfoText = mActivity.getString(R.string.list_build_version_date,
                BuildInfoUtils.getBuildVersion(), buildDate);
        return new AlertDialog.Builder(mActivity)
                .setTitle(R.string.apply_component_dialog_title)
                .setMessage(mActivity.getString(resId, buildInfoText,
                        mActivity.getString(android.R.string.ok)))
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> Utils.triggerUpdate(mActivity, downloadId))
                .setNegativeButton(android.R.string.cancel, null);
    }*/

    private AlertDialog.Builder getCancelInstallationDialog() {
        return new AlertDialog.Builder(mActivity)
                .setMessage(R.string.cancel_installation_dialog_message)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            Intent intent = new Intent(mActivity, ComponentService.class);
                            intent.setAction(ComponentService.ACTION_INSTALL_STOP);
                            mActivity.startService(intent);
                        })
                .setNegativeButton(android.R.string.cancel, null);
    }

    /*private void startActionMode(final ComponentInfo component, final boolean canDelete, View anchor) {
        mSelectedDownload = component.getId();
        notifyItemChanged(component.getId());

        ContextThemeWrapper wrapper = new ContextThemeWrapper(mActivity,
                R.style.AppTheme_PopupMenuOverlapAnchor);
        PopupMenu popupMenu = new PopupMenu(wrapper, anchor, Gravity.NO_GRAVITY,
                R.attr.actionOverflowMenuStyle, 0);
        popupMenu.inflate(R.menu.menu_action_mode);

        MenuBuilder menu = (MenuBuilder) popupMenu.getMenu();
        menu.findItem(R.id.menu_delete_action).setVisible(canDelete);
        menu.findItem(R.id.menu_copy_url).setVisible(component.getAvailableOnline());
        menu.findItem(R.id.menu_export_component).setVisible(
                component.getPersistentStatus() == ComponentStatus.Persistent.VERIFIED);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_delete_action:
                    getDeleteDialog(component.getId()).show();
                    return true;
                case R.id.menu_copy_url:
                    Utils.addToClipboard(mActivity,
                            mActivity.getString(R.string.label_download_url),
                            component.getDownloadUrl(),
                            mActivity.getString(R.string.toast_download_url_copied));
                    return true;
                case R.id.menu_export_component:
                    // TODO: start exporting once the permission has been granted
                    boolean hasPermission = PermissionsUtils.checkAndRequestStoragePermission(
                            mActivity, 0);
                    if (hasPermission) {
                        exportUpdate(component);
                    }
                    return true;
            }
            return false;
        });

        MenuPopupHelper helper = new MenuPopupHelper(wrapper, menu, anchor);
        helper.show();
    }*/

    /*
    private void exportUpdate(ComponentInfo component) {
        File dest = new File(Utils.getExportPath(mActivity), component.getName());
        if (dest.exists()) {
            dest = Utils.appendSequentialNumber(dest);
        }
        Intent intent = new Intent(mActivity, ExportUpdateService.class);
        intent.setAction(ExportUpdateService.ACTION_START_EXPORTING);
        intent.putExtra(ExportUpdateService.EXTRA_SOURCE_FILE, component.getFile());
        intent.putExtra(ExportUpdateService.EXTRA_DEST_FILE, dest);
        mActivity.startService(intent);
    }*/

    /*
    private void showInfoDialog() {
        String messageString = String.format(StringGenerator.getCurrentLocale(mActivity),
                mActivity.getString(R.string.blocked_component_dialog_message),
                Utils.getUpgradeBlockedURL(mActivity));
        SpannableString message = new SpannableString(messageString);
        Linkify.addLinks(message, Linkify.WEB_URLS);
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.blocked_component_dialog_title)
                .setPositiveButton(android.R.string.ok, null)
                .setMessage(message)
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }*/
}