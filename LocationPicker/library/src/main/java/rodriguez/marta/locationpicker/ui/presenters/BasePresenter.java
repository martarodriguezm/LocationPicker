package rodriguez.marta.locationpicker.ui.presenters;

/**
 * Created by martarodriguez on 22/9/15.
 */
public abstract class BasePresenter<V> {
    private V view;

    public BasePresenter() {}

    public void attachView(V view) {
        this.view = view;
        onViewAttached();
    }

    public void detachView() {
        view = null;
    }

    public V getView() {
        return view;
    }

    public abstract void onViewAttached();

}
